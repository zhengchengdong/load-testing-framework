package com.loadtestingframework.jobscript.http.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtestingframework.jobscript.JobContext;
import com.loadtestingframework.jobscript.JobStoppedException;
import com.loadtestingframework.service.JobExecuteService;
import org.springframework.web.socket.TextMessage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebSocketSession {

    private volatile org.springframework.web.socket.WebSocketSession wsSession;
    //用一个队列来存储发送的消息，需要保证线程安全
    private Queue<String> receivedMessages = new ConcurrentLinkedQueue<>();

    public void addReceivedMessage(String message) {
        receivedMessages.add(message);
    }

    public void sendJson(Object sendObj) {
        long jobId = JobContext.getJobId();
        JobExecuteService jobExecuteService = JobContext.getJobExecuteService();
        if (jobExecuteService.isJobStopped(jobId)) {
            throw new JobStoppedException();
        }
        //将发送对象转换为json字符串
        ObjectMapper objectMapper = JobContext.getObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(sendObj);
            //将json字符串发送出去
            TextMessage textMessage = new TextMessage(json);
            wsSession.sendMessage(textMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取一条消息,并解析成对象
    public <T> T receive(Class<T> receiveObjType) {
        return receive(receiveObjType, -1);
    }

    public <T> T receive(Class<T> receiveObjType, long timeout) {
        long jobId = JobContext.getJobId();
        JobExecuteService jobExecuteService = JobContext.getJobExecuteService();
        if (jobExecuteService.isJobStopped(jobId)) {
            throw new JobStoppedException();
        }

        long startTime = System.currentTimeMillis();
        while (true) {
            String msg = receivedMessages.poll();
            if (msg != null) {
                ObjectMapper objectMapper = JobContext.getObjectMapper();
                try {
                    return objectMapper.readValue(msg, receiveObjType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeout) {
                return null;
            }
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    public void close() {
        try {
            wsSession.close();
        } catch (Exception e) {
        }
    }

    public void setWsSession(org.springframework.web.socket.WebSocketSession wsSession) {
        this.wsSession = wsSession;
    }

    public org.springframework.web.socket.WebSocketSession getWsSession() {
        return wsSession;
    }
}

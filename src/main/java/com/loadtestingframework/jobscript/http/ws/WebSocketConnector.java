package com.loadtestingframework.jobscript.http.ws;

import com.loadtestingframework.jobscript.JobContext;
import com.loadtestingframework.jobscript.JobStoppedException;
import com.loadtestingframework.service.JobExecuteService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketConnector {

    public static WebSocketSession connect(String uri) {
        return connect(uri, 5000);
    }

    /**
     * 连接WebSocket
     *
     * @param uri
     * @param overTime 连接超时时间（单位：毫秒）
     * @return WebSocketSession 或者 null 表示连接超时
     */
    public static WebSocketSession connect(String uri, long overTime) {
        long jobId = JobContext.getJobId();
        JobExecuteService jobExecuteService = JobContext.getJobExecuteService();
        if (jobExecuteService.isJobStopped(jobId)) {
            throw new JobStoppedException();
        }

        WebSocketSession session = new WebSocketSession();
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketHandler webSocketHandler = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession wsSession) throws Exception {
                session.setWsSession(wsSession);
            }

            @Override
            protected void handleTextMessage(org.springframework.web.socket.WebSocketSession wsSession, TextMessage message) throws Exception {
                String payload = message.getPayload();
                session.addReceivedMessage(payload);
            }
        };
        webSocketClient.doHandshake(webSocketHandler, uri);
        long startTime = System.currentTimeMillis();
        while (true) {
            if (session.getWsSession() != null) {
                break;
            }
            if (System.currentTimeMillis() - startTime > overTime) {
                return null;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return session;
    }
}

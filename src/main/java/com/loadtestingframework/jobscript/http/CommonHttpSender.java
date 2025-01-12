package com.loadtestingframework.jobscript.http;

import com.loadtestingframework.jobscript.JobContext;
import com.loadtestingframework.jobscript.JobStoppedException;
import com.loadtestingframework.service.JobExecuteService;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CommonHttpSender {
    public static HttpResponse<String> send(HttpRequest request) {
        long jobId = JobContext.getJobId();
        JobExecuteService jobExecuteService = JobContext.getJobExecuteService();
        if (jobExecuteService.isJobStopped(jobId)) {
            throw new JobStoppedException();
        }
        HttpClient httpClient = JobContext.getHttpClient();

        try {
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            int httpCode = response.statusCode();
            jobExecuteService.recordHttpExchange(jobId, startTime, endTime, httpCode);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

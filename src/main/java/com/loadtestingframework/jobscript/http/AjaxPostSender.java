package com.loadtestingframework.jobscript.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtestingframework.jobscript.JobContext;
import com.loadtestingframework.jobscript.JobStoppedException;
import com.loadtestingframework.service.JobExecuteService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AjaxPostSender {

    public static <T> T send(String uriStr, HttpHeader header, Object requestBody, Class<T> responseType) {
        long jobId = JobContext.getJobId();
        JobExecuteService jobExecuteService = JobContext.getJobExecuteService();
        if (jobExecuteService.isJobStopped(jobId)) {
            throw new JobStoppedException();
        }
        HttpClient httpClient = JobContext.getHttpClient();
        ObjectMapper objectMapper = JobContext.getObjectMapper();
        try {
            URI uri = new URI(uriStr);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json");
            if (header != null) {
                List<String> headers = header.getHeaders();
                for (int i = 0; i < headers.size(); i += 2) {
                    requestBuilder.header(headers.get(i), headers.get(i + 1));
                }
            }
            if (requestBody != null) {
                String requestBodyStr = objectMapper.writeValueAsString(requestBody);
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
            } else {
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            }
            HttpRequest request = requestBuilder.build();
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            int httpCode = response.statusCode();
            jobExecuteService.recordHttpExchange(jobId, startTime, endTime, httpCode);
            if (httpCode != 200) {
                return null;
            }
            T responseObj = objectMapper.readValue(response.body(), responseType);
            return responseObj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T send(String uriStr, Object requestBody, Class<T> responseType) {
        return send(uriStr, null, requestBody, responseType);
    }
}

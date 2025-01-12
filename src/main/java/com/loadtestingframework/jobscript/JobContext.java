package com.loadtestingframework.jobscript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtestingframework.service.JobExecuteService;

import java.net.http.HttpClient;

public class JobContext {

    // 定义 ThreadLocal 变量
    private static final ThreadLocal<JobExecuteService> jobExecuteServiceThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Long> jobIdThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<HttpClient> httpClientThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<ObjectMapper> objectMapperThreadLocal = new ThreadLocal<>();

    // 设置 ThreadLocal 变量
    public static void setJobExecuteService(Long jobId, JobExecuteService jobExecuteService) {
        jobIdThreadLocal.set(jobId);
        jobExecuteServiceThreadLocal.set(jobExecuteService);
    }


    // 从 ThreadLocal 获取 JobExecuteService
    public static JobExecuteService getJobExecuteService() {
        return jobExecuteServiceThreadLocal.get();
    }

    // 从 ThreadLocal 获取 jobId
    public static Long getJobId() {
        return jobIdThreadLocal.get();
    }

    // 从 ThreadLocal 获取 HttpClient
    public static HttpClient getHttpClient() {
        HttpClient httpClient = httpClientThreadLocal.get();
        if (httpClient == null) {
            httpClient = HttpClient.newHttpClient();
            httpClientThreadLocal.set(httpClient);
        }
        return httpClient;
    }

    // 从 ThreadLocal 获取 ObjectMapper
    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = objectMapperThreadLocal.get();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapperThreadLocal.set(objectMapper);
        }
        return objectMapper;
    }

    // 清理 ThreadLocal
    public static void clear() {
        jobExecuteServiceThreadLocal.remove();
        jobIdThreadLocal.remove();
        httpClientThreadLocal.remove();
        objectMapperThreadLocal.remove();
    }
}
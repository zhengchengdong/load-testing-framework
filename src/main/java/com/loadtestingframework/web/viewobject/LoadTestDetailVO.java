package com.loadtestingframework.web.viewobject;

import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.entity.TestMetrics;

public class LoadTestDetailVO {
    private String name;
    private int currentJobCount;
    private int setJobCount;
    private int rps;
    private int avgLatency;
    private int failedRequests;
    private int totalRequests;

    public LoadTestDetailVO() {
    }

    public LoadTestDetailVO(LoadTest loadTest, TestMetrics testMetrics, int currentJobCount) {
        this.name = loadTest.getName();
        this.currentJobCount = currentJobCount;
        this.setJobCount = loadTest.getJobAmount();
        this.rps = testMetrics.getRps();
        this.avgLatency = testMetrics.getAvgLatency();
        this.failedRequests = testMetrics.getTotalFailRequest();
        this.totalRequests = testMetrics.getTotalRequest();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCurrentJobCount() {
        return currentJobCount;
    }

    public void setCurrentJobCount(int currentJobCount) {
        this.currentJobCount = currentJobCount;
    }

    public int getSetJobCount() {
        return setJobCount;
    }

    public void setSetJobCount(int setJobCount) {
        this.setJobCount = setJobCount;
    }

    public int getRps() {
        return rps;
    }

    public void setRps(int rps) {
        this.rps = rps;
    }

    public int getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(int avgLatency) {
        this.avgLatency = avgLatency;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(int failedRequests) {
        this.failedRequests = failedRequests;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }
}

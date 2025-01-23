package com.loadtestingframework.web.viewobject;

import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.entity.TestMetrics;

public class LoadTestDetailVO {
    private String name;
    private String jobScriptName;
    private int currentJobCount;
    private int setJobCount;
    private int rps;
    private int avgLatency;
    private int failedRequests;
    private int totalRequests;
    private String description;
    private long startTime;

    public LoadTestDetailVO() {
    }

    public LoadTestDetailVO(LoadTest loadTest, TestMetrics testMetrics) {
        this.name = loadTest.getName();
        this.jobScriptName = loadTest.getJobScriptName();
        this.currentJobCount = loadTest.getCurrentJobAmount();
        this.setJobCount = loadTest.getJobAmount();
        this.rps = testMetrics.getRps();
        this.avgLatency = testMetrics.getAvgLatency();
        this.failedRequests = testMetrics.getTotalFailRequest();
        this.totalRequests = testMetrics.getTotalRequest();
        this.description = loadTest.getDescription();
        this.startTime = loadTest.getStartTime();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobScriptName() {
        return jobScriptName;
    }

    public void setJobScriptName(String jobScriptName) {
        this.jobScriptName = jobScriptName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}

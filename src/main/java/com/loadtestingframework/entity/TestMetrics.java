package com.loadtestingframework.entity;

import java.util.List;

public class TestMetrics {
    private String testName;
    private long startTime;
    private long lastUpdatedTime;
    private int totalRequest;
    private int totalFailRequest;
    private int rps;
    private int sumLatency;
    private int avgLatency;
    private int maxLatency;
    private int minLatency;

    public void update(List<HttpExchange> newExchanges) {
        if (newExchanges.isEmpty()) {
            return;
        }
        for (HttpExchange exchange : newExchanges) {
            totalRequest++;
            if (exchange.getHttpCode() != 200) {
                totalFailRequest++;
            }
            int latency = (int) (exchange.getEndTime() - exchange.getStartTime());
            sumLatency += latency;
            if (latency > maxLatency) {
                maxLatency = latency;
            }
            if (minLatency == 0 || latency < minLatency) {
                minLatency = latency;
            }
            if (exchange.getEndTime() > lastUpdatedTime) {
                lastUpdatedTime = exchange.getEndTime();
            }
            if (startTime == 0 || exchange.getStartTime() < startTime) {
                startTime = exchange.getStartTime();
            }
        }
        avgLatency = sumLatency / totalRequest;
        int totalSeconds = (int) ((lastUpdatedTime - startTime) / 1000);
        rps = totalSeconds == 0 ? totalRequest : totalRequest / totalSeconds;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
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

    public int getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(int maxLatency) {
        this.maxLatency = maxLatency;
    }

    public int getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(int minLatency) {
        this.minLatency = minLatency;
    }

    public int getTotalRequest() {
        return totalRequest;
    }

    public void setTotalRequest(int totalRequest) {
        this.totalRequest = totalRequest;
    }

    public int getTotalFailRequest() {
        return totalFailRequest;
    }

    public void setTotalFailRequest(int totalFailRequest) {
        this.totalFailRequest = totalFailRequest;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public int getSumLatency() {
        return sumLatency;
    }

    public void setSumLatency(int sumLatency) {
        this.sumLatency = sumLatency;
    }


}

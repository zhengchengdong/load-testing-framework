package com.loadtestingframework.entity;

import dml.largescaletaskmanagement.entity.LargeScaleTaskBase;

public class LoadTest extends LargeScaleTaskBase {

    private String name;
    private String jobScriptName;
    private int jobAmount;
    private boolean allJobAdded;

    /**
     * job增加间隔时间（ms）
     */
    private long jobAddInterval;

    /**
     * 每次增加job的数量
     */
    private int jobAddAmount;

    private long lastAddJobTime;
    private int jobAddedAmount;
    private boolean stopped;

    /**
     * 判断是否是逐渐增加job的方式
     */
    public boolean isGraduallyAddJob() {
        return jobAddInterval > 0;
    }

    public boolean isTimeToAddJob(long currTime) {
        if (lastAddJobTime == 0) {
            return true;
        }
        return currTime - lastAddJobTime >= jobAddInterval;
    }

    public int getJobToAddAmount() {
        if (jobAmount - jobAddedAmount < jobAddAmount) {
            return jobAmount - jobAddedAmount;
        }
        return jobAddAmount;
    }

    public void jobAdded(int addedAmount, long currTime) {
        jobAddedAmount += addedAmount;
        if (jobAddedAmount >= jobAmount) {
            allJobAdded = true;
        }
        lastAddJobTime = currTime;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getJobScriptName() {
        return jobScriptName;
    }

    public void setJobScriptName(String jobScriptName) {
        this.jobScriptName = jobScriptName;
    }

    public int getJobAmount() {
        return jobAmount;
    }

    public void setJobAmount(int jobAmount) {
        this.jobAmount = jobAmount;
    }

    public boolean isAllJobAdded() {
        return allJobAdded;
    }

    public void setAllJobAdded(boolean allJobAdded) {
        this.allJobAdded = allJobAdded;
    }

    public long getJobAddInterval() {
        return jobAddInterval;
    }

    public void setJobAddInterval(long jobAddInterval) {
        this.jobAddInterval = jobAddInterval;
    }

    public int getJobAddAmount() {
        return jobAddAmount;
    }

    public void setJobAddAmount(int jobAddAmount) {
        this.jobAddAmount = jobAddAmount;
    }

    public long getLastAddJobTime() {
        return lastAddJobTime;
    }

    public void setLastAddJobTime(long lastAddJobTime) {
        this.lastAddJobTime = lastAddJobTime;
    }

    public int getJobAddedAmount() {
        return jobAddedAmount;
    }

    public void setJobAddedAmount(int jobAddedAmount) {
        this.jobAddedAmount = jobAddedAmount;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}

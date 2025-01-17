package com.loadtestingframework.entity;

public class LoadTest {

    private String name;
    private String jobScriptName;
    private int jobAmount;

    /**
     * job增加间隔时间（ms）
     */
    private long jobAddInterval;

    /**
     * 每次增加job的数量
     */
    private int jobAddAmount;

    private int currentJobAmount;

    private boolean stopped;

    private String description;

    private long startTime;

    public LoadTestLargeScaleTask createLargeScaleTask() {
        LoadTestLargeScaleTask task = new LoadTestLargeScaleTask();
        task.setName(name);
        task.setJobScriptName(jobScriptName);
        task.setJobAmount(jobAmount);
        task.setJobAddInterval(jobAddInterval);
        task.setJobAddAmount(jobAddAmount);
        return task;
    }

    public boolean isGraduallyAddJob() {
        return jobAddInterval > 0;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public int getCurrentJobAmount() {
        return currentJobAmount;
    }

    public void setCurrentJobAmount(int currentJobAmount) {
        this.currentJobAmount = currentJobAmount;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
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

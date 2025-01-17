package com.loadtestingframework.entity;

public class LoadTestJob {
    private long id;
    private String testName;
    private String jobScriptName;
    private boolean started;
    private boolean finished;

    public LoadTestLargeScaleTaskSegment createLargeScaleTaskSegment() {
        LoadTestLargeScaleTaskSegment segment = new LoadTestLargeScaleTaskSegment();
        segment.setId(id);
        segment.setTestName(testName);
        segment.setJobScriptName(jobScriptName);
        return segment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getJobScriptName() {
        return jobScriptName;
    }

    public void setJobScriptName(String jobScriptName) {
        this.jobScriptName = jobScriptName;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }


}

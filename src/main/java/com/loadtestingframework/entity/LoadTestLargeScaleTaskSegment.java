package com.loadtestingframework.entity;

import dml.largescaletaskmanagement.entity.LargeScaleTaskSegmentBase;

public class LoadTestLargeScaleTaskSegment extends LargeScaleTaskSegmentBase {
    private long jobId;
    private String testName;
    private String jobScriptName;

    @Override
    public void setId(Object id) {
        this.jobId = (long) id;
    }

    @Override
    public Object getId() {
        return jobId;
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
}

package com.loadtestingframework.entity;

import dml.largescaletaskmanagement.entity.LargeScaleTaskSegmentBase;

public class LoadTestJob extends LargeScaleTaskSegmentBase {
    private long id;
    private String testName;
    private String jobScriptName;

    @Override
    public void setId(Object id) {
        this.id = (long) id;
    }

    @Override
    public Object getId() {
        return id;
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

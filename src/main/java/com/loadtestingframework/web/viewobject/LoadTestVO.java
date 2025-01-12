package com.loadtestingframework.web.viewobject;

import com.loadtestingframework.entity.LoadTest;

import java.util.List;
import java.util.stream.Collectors;

public class LoadTestVO {
    private String id;
    private String name;
    private String scriptName;
    private int jobCount;
    private String submitMethod;

    public static List<LoadTestVO> fromLoadTests(List<LoadTest> loadTests) {
        return loadTests.stream().map(LoadTestVO::new).collect(Collectors.toList());
    }

    public LoadTestVO() {
    }

    public LoadTestVO(LoadTest loadTest) {
        this.id = loadTest.getName();
        this.name = loadTest.getName();
        this.scriptName = loadTest.getJobScriptName();
        this.jobCount = loadTest.getJobAmount();
        this.submitMethod = loadTest.isGraduallyAddJob() ? "gradually" : "full";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public int getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }

    public String getSubmitMethod() {
        return submitMethod;
    }

    public void setSubmitMethod(String submitMethod) {
        this.submitMethod = submitMethod;
    }
}

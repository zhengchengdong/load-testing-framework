package com.loadtestingframework.web.viewobject;

import java.util.List;
import java.util.stream.Collectors;

public class JobScriptVO {
    private String id;
    private String name;

    public static List<JobScriptVO> fromJobScripts(List<String> jobScripts) {
        return jobScripts.stream().map(JobScriptVO::new).collect(Collectors.toList());
    }

    public JobScriptVO() {
    }

    public JobScriptVO(String scriptName) {
        this.id = scriptName;
        this.name = scriptName;
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
}

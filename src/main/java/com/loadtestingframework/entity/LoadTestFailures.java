package com.loadtestingframework.entity;

import java.util.ArrayList;
import java.util.List;

public class LoadTestFailures {
    private String testName;
    private List<HttpExchange> failures;

    public LoadTestFailures() {
    }

    public LoadTestFailures(String testName) {
        this.testName = testName;
    }

    public void addFailure(HttpExchange httpExchange) {
        if (failures == null) {
            failures = new ArrayList<>();
        }
        failures.add(httpExchange);
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public List<HttpExchange> getFailures() {
        return failures;
    }

    public void setFailures(List<HttpExchange> failures) {
        this.failures = failures;
    }


}

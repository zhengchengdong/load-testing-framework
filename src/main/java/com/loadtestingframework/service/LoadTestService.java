package com.loadtestingframework.service;

import com.loadtestingframework.entity.HttpExchange;
import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.entity.LoadTestJob;
import com.loadtestingframework.entity.TestMetrics;
import com.loadtestingframework.repository.*;
import dml.largescaletaskmanagement.entity.LargeScaleTaskSegment;
import dml.largescaletaskmanagement.repository.LargeScaleTaskRepository;
import dml.largescaletaskmanagement.repository.LargeScaleTaskSegmentRepository;
import dml.largescaletaskmanagement.service.LargeScaleTaskService;
import dml.largescaletaskmanagement.service.repositoryset.LargeScaleTaskServiceRepositorySet;
import dml.largescaletaskmanagement.service.result.TakeTaskSegmentToExecuteResult;
import erp.annotation.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoadTestService implements LargeScaleTaskServiceRepositorySet {

    @Autowired
    private LoadTestRepository loadTestRepository;

    @Autowired
    private LoadTestJobRepository loadTestJobRepository;

    @Autowired
    private LoadTestJobIdGeneratorRepository loadTestJobIdGeneratorRepository;

    @Autowired
    private TestMetricsRepository testMetricsRepository;

    @Autowired
    private HttpExchangeRepository httpExchangeRepository;

    @Process
    public void createTest(String testName, String jobScriptName, int jobAmount, long currTime) {
        LoadTest loadTest = new LoadTest();
        loadTest.setJobScriptName(jobScriptName);
        loadTest.setJobAmount(jobAmount);
        LargeScaleTaskService.createTask(this,
                testName, loadTest, currTime);
        loadTest.readyToProcess();
    }

    @Process
    public void createGraduallyTest(String testName, String jobScriptName, int jobAmount,
                                    int jobAddAmount, long jobAddInterval, long currTime) {
        LoadTest loadTest = new LoadTest();
        loadTest.setName(testName);
        loadTest.setJobScriptName(jobScriptName);
        loadTest.setJobAmount(jobAmount);
        loadTest.setJobAddAmount(jobAddAmount);
        loadTest.setJobAddInterval(jobAddInterval);
        LargeScaleTaskService.createTask(this,
                testName, loadTest, currTime);
        loadTest.readyToProcess();
    }

    public List<String> getAllTestNames() {
        return loadTestRepository.getAllTestNames();
    }

    @Process
    public LoadTest addJobForTest(String testName, long currTime) {
        LoadTest loadTest = loadTestRepository.take(testName);
        if (loadTest.isStopped() || loadTest.isAllJobAdded()) {
            return loadTest;
        }
        if (!loadTest.isGraduallyAddJob()) {
            for (int i = 0; i < loadTest.getJobAmount(); i++) {
                LoadTestJob loadTestJob = new LoadTestJob();
                loadTestJob.setId(loadTestJobIdGeneratorRepository.take().generateId());
                loadTestJob.setTestName(testName);
                loadTestJob.setJobScriptName(loadTest.getJobScriptName());
                LargeScaleTaskService.addTaskSegment(this,
                        testName, loadTestJob);
            }
            loadTest.jobAdded(loadTest.getJobAmount(), currTime);
        } else {
            if (loadTest.isTimeToAddJob(currTime)) {
                int jobToAddAmount = loadTest.getJobToAddAmount();
                for (int i = 0; i < jobToAddAmount; i++) {
                    LoadTestJob loadTestJob = new LoadTestJob();
                    loadTestJob.setId(loadTestJobIdGeneratorRepository.take().generateId());
                    loadTestJob.setTestName(testName);
                    loadTestJob.setJobScriptName(loadTest.getJobScriptName());
                    LargeScaleTaskService.addTaskSegment(this,
                            testName, loadTestJob);
                }
                loadTest.jobAdded(jobToAddAmount, currTime);
            }
        }
        return loadTest;
    }

    @Process
    public LoadTestJob takeJobToExecute(String testName, long currTime) {
        TakeTaskSegmentToExecuteResult result = LargeScaleTaskService.takeTaskSegmentToExecute(this,
                testName, currTime, 1, 1);
        LargeScaleTaskSegment taskSegment = result.getTaskSegment();
        if (taskSegment == null) {
            return null;
        }
        LargeScaleTaskService.completeTaskSegment(this,
                taskSegment.getId());
        return (LoadTestJob) taskSegment;
    }

    @Process
    public void stopTest(String testName) {
        LoadTest loadTest = loadTestRepository.take(testName);
        loadTest.setStopped(true);
    }

    @Process
    public void calculateTestMetrics(String testName) {
        TestMetrics testMetrics = new TestMetrics();
        testMetrics.setTestName(testName);
        testMetrics = testMetricsRepository.takeOrPutIfAbsent(testName, testMetrics);
        List<Long> allHttpExchangeIds = httpExchangeRepository.getAllIds();
        List<HttpExchange> httpExchangesForTest = new ArrayList<>();
        for (long httpExchangeId : allHttpExchangeIds) {
            HttpExchange httpExchange = httpExchangeRepository.find(httpExchangeId);
            if (httpExchange.getTestName().equals(testName)) {
                httpExchangesForTest.add(httpExchange);
                httpExchangeRepository.remove(httpExchangeId);
            }
        }
        testMetrics.update(httpExchangesForTest);
    }

    @Process
    public void deleteTest(String testName) {
        loadTestRepository.remove(testName);
        testMetricsRepository.remove(testName);
    }

    public TestMetrics getTestMetrics(String testName) {
        return testMetricsRepository.find(testName);
    }

    public LoadTest getLoadTest(String testName) {
        return loadTestRepository.find(testName);
    }

    public List<LoadTest> getAllTests() {
        List<LoadTest> loadTests = new ArrayList<>();
        List<String> allTestNames = loadTestRepository.getAllTestNames();
        for (String testName : allTestNames) {
            loadTests.add(loadTestRepository.find(testName));
        }
        return loadTests;
    }

    @Override
    public LargeScaleTaskRepository getLargeScaleTaskRepository() {
        return loadTestRepository;
    }

    @Override
    public LargeScaleTaskSegmentRepository getLargeScaleTaskSegmentRepository() {
        return loadTestJobRepository;
    }

    public void setLoadTestRepository(LoadTestRepository loadTestRepository) {
        this.loadTestRepository = loadTestRepository;
    }

    public void setLoadTestJobRepository(LoadTestJobRepository loadTestJobRepository) {
        this.loadTestJobRepository = loadTestJobRepository;
    }

    public void setLoadTestJobIdGeneratorRepository(LoadTestJobIdGeneratorRepository loadTestJobIdGeneratorRepository) {
        this.loadTestJobIdGeneratorRepository = loadTestJobIdGeneratorRepository;
    }

    public void setTestMetricsRepository(TestMetricsRepository testMetricsRepository) {
        this.testMetricsRepository = testMetricsRepository;
    }

    public void setHttpExchangeRepository(HttpExchangeRepository httpExchangeRepository) {
        this.httpExchangeRepository = httpExchangeRepository;
    }


}

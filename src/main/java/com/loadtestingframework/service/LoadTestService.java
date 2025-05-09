package com.loadtestingframework.service;

import com.loadtestingframework.entity.*;
import com.loadtestingframework.repository.*;
import dml.largescaletaskmanagement.entity.LargeScaleTaskSegment;
import dml.largescaletaskmanagement.repository.LargeScaleTaskRepository;
import dml.largescaletaskmanagement.repository.LargeScaleTaskSegmentRepository;
import dml.largescaletaskmanagement.service.LargeScaleTaskService;
import dml.largescaletaskmanagement.service.repositoryset.LargeScaleTaskServiceRepositorySet;
import dml.largescaletaskmanagement.service.result.TakeTaskSegmentToExecuteResult;
import erp.annotation.Process;
import erp.redis.pipeline.RedisPipeline;
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
    private LoadTestLargeScaleTaskRepository loadTestLargeScaleTaskRepository;

    @Autowired
    private LoadTestLargeScaleTaskSegmentRepository loadTestLargeScaleTaskSegmentRepository;

    @Autowired
    private LoadTestJobIdGeneratorRepository loadTestJobIdGeneratorRepository;


    @Autowired
    private TestMetricsRepository testMetricsRepository;

    @Autowired
    private HttpExchangeRepository httpExchangeRepository;

    @Autowired
    private FailureHttpExchangeRepository failureHttpExchangeRepository;


    @Process
    @RedisPipeline
    public void createTest(String testName, String jobScriptName, int jobAmount, String description, long currTime) {
        LoadTest loadTest = new LoadTest();
        loadTest.setName(testName);
        loadTest.setJobScriptName(jobScriptName);
        loadTest.setJobAmount(jobAmount);
        loadTest.setDescription(description);
        loadTest.setStartTime(currTime);
        loadTestRepository.put(loadTest);
        LoadTestLargeScaleTask task = loadTest.createLargeScaleTask();
        LargeScaleTaskService.createTask(this,
                testName, task, currTime);
        LargeScaleTaskService.setTaskReadyToProcess(this,
                testName);
    }


    @Process
    @RedisPipeline
    public void createGraduallyTest(String testName, String jobScriptName, int jobAmount,
                                    int jobAddAmount, long jobAddInterval, String description, long currTime) {
        LoadTest loadTest = new LoadTest();
        loadTest.setName(testName);
        loadTest.setJobScriptName(jobScriptName);
        loadTest.setJobAmount(jobAmount);
        loadTest.setJobAddAmount(jobAddAmount);
        loadTest.setJobAddInterval(jobAddInterval);
        loadTest.setDescription(description);
        loadTest.setStartTime(currTime);
        loadTestRepository.put(loadTest);
        LoadTestLargeScaleTask task = loadTest.createLargeScaleTask();
        LargeScaleTaskService.createTask(this,
                testName, task, currTime);
        LargeScaleTaskService.setTaskReadyToProcess(this,
                testName);
    }

    public List<String> getAllTestNames() {
        return loadTestRepository.getAllTestNames();
    }

    @Process
    @RedisPipeline
    public boolean addJobForTest(String testName, long currTime) {
        LoadTest loadTest = loadTestRepository.find(testName);
        if (loadTest == null) {
            return false;
        }
        LoadTestLargeScaleTask loadTestTask = loadTestLargeScaleTaskRepository.take(testName);
        if (loadTest.isStopped() || loadTestTask.isAllJobAdded()) {
            return false;
        }
        if (!loadTestTask.isGraduallyAddJob()) {
            for (int i = 0; i < loadTestTask.getJobAmount(); i++) {
                LoadTestJob loadTestJob = new LoadTestJob();
                loadTestJob.setId(loadTestJobIdGeneratorRepository.take().generateId());
                loadTestJob.setTestName(testName);
                loadTestJob.setJobScriptName(loadTestTask.getJobScriptName());
                loadTestJobRepository.put(loadTestJob);
                LoadTestLargeScaleTaskSegment taskSegment = loadTestJob.createLargeScaleTaskSegment();
                LargeScaleTaskService.addTaskSegment(this,
                        testName, taskSegment);
            }
            loadTestTask.jobAdded(loadTestTask.getJobAmount(), currTime);
        } else {
            if (loadTestTask.isTimeToAddJob(currTime)) {
                int jobToAddAmount = loadTestTask.getJobToAddAmount();
                for (int i = 0; i < jobToAddAmount; i++) {
                    LoadTestJob loadTestJob = new LoadTestJob();
                    loadTestJob.setId(loadTestJobIdGeneratorRepository.take().generateId());
                    loadTestJob.setTestName(testName);
                    loadTestJob.setJobScriptName(loadTestTask.getJobScriptName());
                    loadTestJobRepository.put(loadTestJob);
                    LoadTestLargeScaleTaskSegment taskSegment = loadTestJob.createLargeScaleTaskSegment();
                    LargeScaleTaskService.addTaskSegment(this,
                            testName, taskSegment);
                }
                loadTestTask.jobAdded(jobToAddAmount, currTime);
            }
        }
        return true;
    }

    @Process
    @RedisPipeline
    public LoadTestLargeScaleTaskSegment takeJobToExecute(String testName, long currTime) {
        TakeTaskSegmentToExecuteResult result = LargeScaleTaskService.takeTaskSegmentToExecute(this,
                testName, currTime, 1, 1);
        LargeScaleTaskSegment taskSegment = result.getTaskSegment();
        if (taskSegment == null) {
            return null;
        }
        LargeScaleTaskService.completeTaskSegment(this,
                taskSegment.getId());
        return (LoadTestLargeScaleTaskSegment) taskSegment;
    }

    @Process
    @RedisPipeline
    public void stopTest(String testName) {
        LoadTest loadTest = loadTestRepository.take(testName);
        loadTest.setStopped(true);
    }

    @Process
    @RedisPipeline
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
    @RedisPipeline
    public void deleteTest(String testName) {
        loadTestRepository.remove(testName);
        loadTestLargeScaleTaskRepository.remove(testName);
        testMetricsRepository.remove(testName);
        List<Long> allFailureHttpExchangeIds = failureHttpExchangeRepository.getAllIds();
        for (long httpExchangeId : allFailureHttpExchangeIds) {
            HttpExchange httpExchange = failureHttpExchangeRepository.find(httpExchangeId);
            if (httpExchange.getTestName().equals(testName)) {
                failureHttpExchangeRepository.remove(httpExchangeId);
            }
        }
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
        //loadTests 按 startTime 降序排列
        loadTests.sort((o1, o2) -> (int) (o2.getStartTime() - o1.getStartTime()));
        return loadTests;
    }

    public List<HttpExchange> getTestFailures(String testName) {
        List<Long> allHttpExchangeIds = failureHttpExchangeRepository.getAllIds();
        List<HttpExchange> failures = new ArrayList<>();
        for (long httpExchangeId : allHttpExchangeIds) {
            HttpExchange httpExchange = failureHttpExchangeRepository.find(httpExchangeId);
            if (httpExchange.getTestName().equals(testName)) {
                failures.add(httpExchange);
            }
        }
        //按开始时间升序排列
        failures.sort((o1, o2) -> (int) (o1.getStartTime() - o2.getStartTime()));
        return failures;
    }

    @Process
    @RedisPipeline
    public void calculateCurrentJobAmount(String testName) {
        LoadTest loadTest = loadTestRepository.find(testName);
        if (loadTest == null) {
            return;
        }
        if (loadTest.isStopped() && loadTest.getCurrentJobAmount() == 0) {
            return;
        }
        List<Long> allJobIds = loadTestJobRepository.getAllIds();
        int currentJobAmount = 0;
        for (long jobId : allJobIds) {
            LoadTestJob loadTestJob = loadTestJobRepository.find(jobId);
            if (loadTestJob == null) {
                continue;
            }
            if (loadTestJob.getTestName().equals(testName)) {
                currentJobAmount++;
            }
        }
        loadTest = loadTestRepository.take(testName);
        loadTest.setCurrentJobAmount(currentJobAmount);
    }

    public List<Long> getAllJobIds() {
        return loadTestJobRepository.getAllIds();
    }

    @Process
    @RedisPipeline
    public void deleteJobIfTestNotExist(Long jobId) {
        LoadTestJob loadTestJob = loadTestJobRepository.find(jobId);
        if (loadTestJob == null) {
            return;
        }
        LoadTest loadTest = loadTestRepository.find(loadTestJob.getTestName());
        if (loadTest == null) {
            loadTestJobRepository.remove(jobId);
        }
    }

    public LoadTestLargeScaleTask getLoadTestLargeScaleTask(String testName) {
        return loadTestLargeScaleTaskRepository.find(testName);
    }

    @Override
    public LargeScaleTaskRepository getLargeScaleTaskRepository() {
        return loadTestLargeScaleTaskRepository;
    }

    @Override
    public LargeScaleTaskSegmentRepository getLargeScaleTaskSegmentRepository() {
        return loadTestLargeScaleTaskSegmentRepository;
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

    public void setLoadTestLargeScaleTaskRepository(LoadTestLargeScaleTaskRepository loadTestLargeScaleTaskRepository) {
        this.loadTestLargeScaleTaskRepository = loadTestLargeScaleTaskRepository;
    }

    public void setLoadTestLargeScaleTaskSegmentRepository(LoadTestLargeScaleTaskSegmentRepository loadTestLargeScaleTaskSegmentRepository) {
        this.loadTestLargeScaleTaskSegmentRepository = loadTestLargeScaleTaskSegmentRepository;
    }


}

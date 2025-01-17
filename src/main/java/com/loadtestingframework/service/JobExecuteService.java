package com.loadtestingframework.service;

import com.loadtestingframework.entity.*;
import com.loadtestingframework.jobscript.JobContext;
import com.loadtestingframework.jobscript.JobStoppedException;
import com.loadtestingframework.repository.*;
import dml.id.entity.IdGenerator;
import erp.annotation.Process;
import erp.repository.TakeEntityException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class JobExecuteService {

    @Autowired
    private JobExecuteStateRepository jobExecuteStateRepository;

    @Autowired
    private HttpExchangeIdGeneratorRepository httpExchangeIdGeneratorRepository;

    @Autowired
    private HttpExchangeRepository httpExchangeRepository;

    @Autowired
    private LoadTestRepository loadTestRepository;

    @Autowired
    private LoadTestJobRepository loadTestJobRepository;


    /**
     * 线程池
     */
    private ExecutorService threadPool = Executors.newCachedThreadPool();



    public void executeJob(LoadTestLargeScaleTaskSegment taskSegment) {
        long jobId = (long) taskSegment.getId();
        String testName = taskSegment.getTestName();
        String jobScriptName = taskSegment.getJobScriptName();
        startJob(jobId,testName);
        threadPool.execute(() -> {
            JobContext.setJobExecuteService(jobId, this);
            try {
                Class<?> jobScriptClass = Class.forName("com.loadtestingframework.jobscript." + jobScriptName);
                Method runMethod = jobScriptClass.getMethod("executeJob");
                runMethod.invoke(null);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException
                        && e.getCause() instanceof JobStoppedException) {
                    return;
                }
                e.printStackTrace();
            } finally {
                JobContext.clear();
                finishJob(jobId);
            }
        });
    }

    @Process
    private void startJob(long jobId,String testName) {
        JobExecuteState jobExecuteState = new JobExecuteState();
        jobExecuteState.setJobId(jobId);
        jobExecuteState.setTestName(testName);
        jobExecuteStateRepository.put(jobExecuteState);
        LoadTestJob loadTestJob = loadTestJobRepository.take(jobId);
        loadTestJob.setStarted(true);
    }

    @Process
    private void finishJob(long jobId) {
        jobExecuteStateRepository.remove(jobId);
        LoadTestJob loadTestJob = loadTestJobRepository.take(jobId);
        loadTestJob.setFinished(true);
    }

    public boolean isJobScriptValid(String jobScriptName) {
        String jobScriptClassName = "com.loadtestingframework.jobscript." + jobScriptName;
        try {
            Class<?> jobScriptClass = Class.forName(jobScriptClassName);
            //检查是否存在静态方法 “void run(long jobId, JobExecuteService jobExecuteService)”
            Method runMethod = jobScriptClass.getMethod("executeJob");
            int modifiers = runMethod.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }
    }

    public List<String> getAllScripts() {
        //获取"com.loadtestingframework.jobscript."包下的所有类名，并去掉"com.loadtestingframework.jobscript."前缀，同时验证是否是有效的job script，返回有效的job script
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages("com.loadtestingframework.jobscript")
                .scan()) {
            return scanResult.getAllClasses().stream()
                    .map(ClassInfo::getSimpleName)
                    .filter(this::isJobScriptValid)
                    .collect(Collectors.toList());
        }
    }

    @Process
    public boolean isJobStopped(long jobId) {
        JobExecuteState jobExecuteState = jobExecuteStateRepository.find(jobId);
        return jobExecuteState == null;
    }


    public void recordHttpExchange(long jobId, long startTime, long endTime, int httpCode) {
        threadPool.execute(() -> doRecordHttpExchange(generateHttpExchangeId(), jobId, startTime, endTime, httpCode));
    }

    @Process
    private void doRecordHttpExchange(long id, long jobId, long startTime, long endTime, int httpCode) {
        JobExecuteState jobExecuteState = jobExecuteStateRepository.find(jobId);
        if (jobExecuteState == null) {
            return;
        }
        HttpExchange httpExchange = new HttpExchange();
        httpExchange.setId(id);
        httpExchange.setTestName(jobExecuteState.getTestName());
        httpExchange.setJobId(jobId);
        httpExchange.setStartTime(startTime);
        httpExchange.setEndTime(endTime);
        httpExchange.setHttpCode(httpCode);
        httpExchangeRepository.put(httpExchange);
    }

    @Process
    private long generateHttpExchangeId() {
        IdGenerator<Long> httpExchangeIdGenerator;
        while (true) {
            try {
                httpExchangeIdGenerator = httpExchangeIdGeneratorRepository.take();
                break;
            } catch (TakeEntityException e) {
                continue;
            }
        }
        return httpExchangeIdGenerator.generateId();
    }

    @Process
    public void stopJobsForStoppedTest(String testName) {
        LoadTest loadTest = loadTestRepository.find(testName);
        if (!loadTest.isStopped()) {
            return;
        }
        List<Long> allJobIds = jobExecuteStateRepository.queryAllIds();
        for (long jobId : allJobIds) {
            JobExecuteState jobExecuteState = jobExecuteStateRepository.find(jobId);
            if (jobExecuteState.getTestName().equals(testName)) {
                jobExecuteStateRepository.remove(jobId);
            }
        }
    }

    public void setJobExecuteStateRepository(JobExecuteStateRepository jobExecuteStateRepository) {
        this.jobExecuteStateRepository = jobExecuteStateRepository;
    }

    public void setHttpExchangeIdGeneratorRepository(HttpExchangeIdGeneratorRepository httpExchangeIdGeneratorRepository) {
        this.httpExchangeIdGeneratorRepository = httpExchangeIdGeneratorRepository;
    }

    public void setHttpExchangeRepository(HttpExchangeRepository httpExchangeRepository) {
        this.httpExchangeRepository = httpExchangeRepository;
    }

    public void setLoadTestRepository(LoadTestRepository loadTestRepository) {
        this.loadTestRepository = loadTestRepository;
    }

    public void setLoadTestJobRepository(LoadTestJobRepository loadTestJobRepository) {
        this.loadTestJobRepository = loadTestJobRepository;
    }
}

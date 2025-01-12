package com.loadtestingframework.scheduled;

import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.entity.LoadTestJob;
import com.loadtestingframework.service.JobExecuteService;
import com.loadtestingframework.service.LoadTestService;
import erp.repository.TakeEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledController {

    @Autowired
    private LoadTestService loadTestService;

    @Autowired
    private JobExecuteService jobExecuteService;

    /**
     * 给测试添加Job
     */
    @Scheduled(fixedRate = 1000)
    public void addJobForTest() {
        boolean noTestNeedToAddJob = false;
        while (!noTestNeedToAddJob) {
            noTestNeedToAddJob = true;
            List<String> allTestNames = loadTestService.getAllTestNames();
            for (String testName : allTestNames) {
                try {
                    LoadTest loadTest = loadTestService.addJobForTest(testName, System.currentTimeMillis());
                    if (!loadTest.isStopped() && !loadTest.isAllJobAdded()) {
                        noTestNeedToAddJob = false;
                    }
                } catch (TakeEntityException e) {
                }
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取Job，并执行
     */
    @Scheduled(fixedRate = 10)
    public void getAndExecuteJob() {
        List<String> allTestNames = loadTestService.getAllTestNames();
        for (String testName : allTestNames) {
            while (true) {
                LoadTestJob loadTestJob = null;
                try {
                    loadTestJob = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
                    if (loadTestJob == null) {
                        break;
                    }
                } catch (TakeEntityException e) {
                    continue;
                }
                jobExecuteService.executeJob(loadTestJob);
            }
        }
    }

    /**
     * 计算测试指标
     */
    @Scheduled(fixedRate = 1000)
    public void calculateTestMetrics() {
        List<String> allTestNames = loadTestService.getAllTestNames();
        for (String testName : allTestNames) {
            try {
                loadTestService.calculateTestMetrics(testName);
            } catch (TakeEntityException e) {
            }
        }
    }

    /**
     * 停止Job
     */
    @Scheduled(fixedRate = 1000)
    public void stopJobsForStoppedTest() {
        List<String> allTestNames = loadTestService.getAllTestNames();
        for (String testName : allTestNames) {
            jobExecuteService.stopJobsForStoppedTest(testName);
        }
    }
}

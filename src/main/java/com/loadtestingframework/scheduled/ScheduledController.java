package com.loadtestingframework.scheduled;

import com.loadtestingframework.entity.LoadTestLargeScaleTaskSegment;
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
    @Scheduled(fixedRate = 30)
    public void addJobForTest() {
        boolean noTestNeedToAddJob = false;
        while (!noTestNeedToAddJob) {
            noTestNeedToAddJob = true;
            List<String> allTestNames = loadTestService.getAllTestNames();
            for (String testName : allTestNames) {
                try {
                    boolean needToAdd = loadTestService.addJobForTest(testName, System.currentTimeMillis());
                    if (needToAdd) {
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
    @Scheduled(fixedRate = 1000)
    public void getAndExecuteJob() {
        List<String> allTestNames = loadTestService.getAllTestNames();
        for (String testName : allTestNames) {
            while (true) {
                LoadTestLargeScaleTaskSegment loadTestTaskSegment = null;
                try {
                    loadTestTaskSegment = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
                    if (loadTestTaskSegment == null) {
                        break;
                    }
                } catch (TakeEntityException e) {
                    continue;
                }
                jobExecuteService.executeJob(loadTestTaskSegment);
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
     * 计算当前Job数量
     */
    @Scheduled(fixedRate = 1000)
    public void calculateCurrentJobAmount() {
        List<String> allTestNames = loadTestService.getAllTestNames();
        for (String testName : allTestNames) {
            try {
                loadTestService.calculateCurrentJobAmount(testName);
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
            try {
                jobExecuteService.stopJobsForStoppedTest(testName);
            } catch (TakeEntityException e) {
            }
        }
    }

    /**
     * 删除已删除的测试的Job
     */
    @Scheduled(fixedRate = 10000)
    public void deleteJobsForDeletedTest() {
        List<Long> allJobIds = loadTestService.getAllJobIds();
        for (Long jobId : allJobIds) {
            loadTestService.deleteJobIfTestNotExist(jobId);
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

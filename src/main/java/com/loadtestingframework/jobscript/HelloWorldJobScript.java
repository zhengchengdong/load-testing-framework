package com.loadtestingframework.jobscript;

import com.loadtestingframework.service.JobExecuteService;

public class HelloWorldJobScript {
    public static void executeJob() {
        long jobId = JobContext.getJobId();
        JobExecuteService jobExecuteService = JobContext.getJobExecuteService();

        while (true) {
            if (jobExecuteService.isJobStopped(jobId)) {
                throw new JobStoppedException();
            }
            long startTime = System.currentTimeMillis();
            System.out.println("Hello, World!");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            long endTime = System.currentTimeMillis();
            int httpCode = 200;
            jobExecuteService.recordHttpExchange(jobId, startTime, endTime, "", httpCode, "");
        }
    }
}

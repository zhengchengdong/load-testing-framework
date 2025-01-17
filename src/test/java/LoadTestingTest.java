import com.loadtestingframework.entity.LoadTestLargeScaleTask;
import com.loadtestingframework.entity.LoadTestLargeScaleTaskSegment;
import com.loadtestingframework.entity.TestMetrics;
import com.loadtestingframework.repository.*;
import com.loadtestingframework.repository.impl.MemHttpExchangeRepository;
import com.loadtestingframework.repository.impl.RedisLoadTestJobRepository;
import com.loadtestingframework.repository.impl.RedisLoadTestRepository;
import com.loadtestingframework.service.JobExecuteService;
import com.loadtestingframework.service.LoadTestService;
import dml.id.entity.IdGenerator;
import dml.id.entity.SnowflakeIdGenerator;
import erp.ERP;
import erp.redis.RedisRepository;
import erp.repository.factory.RepositoryFactory;
import erp.repository.factory.SingletonRepositoryFactory;
import erp.repository.impl.mem.MemSingletonRepository;
import org.junit.Test;

public class LoadTestingTest {
    @Test
    public void test() {
        ERP.useAnnotation();
        String testName = "HelloWorldTest";
        String jobScriptName = "HelloWorldJobScript";

        LoadTestRepository loadTestRepository = new RedisLoadTestRepository(null);
        LoadTestJobRepository loadTestJobRepository = new RedisLoadTestJobRepository(null);
        LoadTestLargeScaleTaskRepository loadTestLargeScaleTaskRepository = RepositoryFactory.newInstance(
                LoadTestLargeScaleTaskRepository.class, new RedisRepository(null, LoadTestLargeScaleTask.class));
        LoadTestLargeScaleTaskSegmentRepository loadTestLargeScaleTaskSegmentRepository = RepositoryFactory.newInstance(
                LoadTestLargeScaleTaskSegmentRepository.class, new RedisRepository(null, LoadTestLargeScaleTaskSegment.class));
        LoadTestJobIdGeneratorRepository loadTestJobIdGeneratorRepository =
                SingletonRepositoryFactory.newInstance(LoadTestJobIdGeneratorRepository.class,
                        new MemSingletonRepository<IdGenerator<Long>>(new SnowflakeIdGenerator(1L),
                                "loadTestJobIdGeneratorRepository"));
        TestMetricsRepository testMetricsRepository = RepositoryFactory.newInstance(TestMetricsRepository.class,
                new RedisRepository(null, TestMetrics.class));
        HttpExchangeRepository httpExchangeRepository = new MemHttpExchangeRepository();
        JobExecuteStateRepository jobExecuteStateRepository = new JobExecuteStateRepository();
        HttpExchangeIdGeneratorRepository httpExchangeIdGeneratorRepository =
                SingletonRepositoryFactory.newInstance(HttpExchangeIdGeneratorRepository.class,
                        new MemSingletonRepository<IdGenerator<Long>>(new SnowflakeIdGenerator(1L),
                                "httpExchangeIdGeneratorRepository"));

        LoadTestService loadTestService = new LoadTestService();
        loadTestService.setLoadTestJobRepository(loadTestJobRepository);
        loadTestService.setLoadTestJobIdGeneratorRepository(loadTestJobIdGeneratorRepository);
        loadTestService.setLoadTestRepository(loadTestRepository);
        loadTestService.setTestMetricsRepository(testMetricsRepository);
        loadTestService.setHttpExchangeRepository(httpExchangeRepository);
        loadTestService.setLoadTestLargeScaleTaskRepository(loadTestLargeScaleTaskRepository);
        loadTestService.setLoadTestLargeScaleTaskSegmentRepository(loadTestLargeScaleTaskSegmentRepository);
        JobExecuteService jobExecuteService = new JobExecuteService();
        jobExecuteService.setJobExecuteStateRepository(jobExecuteStateRepository);
        jobExecuteService.setHttpExchangeIdGeneratorRepository(httpExchangeIdGeneratorRepository);
        jobExecuteService.setHttpExchangeRepository(httpExchangeRepository);
        jobExecuteService.setLoadTestRepository(loadTestRepository);
        jobExecuteService.setLoadTestJobRepository(loadTestJobRepository);

        //创建一个测试，指定要执行哪个Job脚本,Job增加方式，Job总数（HelloWorld,全量，2个）
        boolean jobScriptValid = jobExecuteService.isJobScriptValid(jobScriptName);
        assert jobScriptValid;
        loadTestService.createTest(testName, jobScriptName, 2, "", System.currentTimeMillis());
        //定时线程来给测试添加Job（全量2个）
        loadTestService.addJobForTest(testName, System.currentTimeMillis());
        LoadTestLargeScaleTask loadTestTask1 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask1.isAllJobAdded();
        //定时线程来获取一个Job，并执行
        LoadTestLargeScaleTaskSegment taskSegment1 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(taskSegment1);
        //定时线程来获取另一个Job，并执行
        LoadTestLargeScaleTaskSegment taskSegment2 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(taskSegment2);
        //让时间流逝1.1秒，确保Job执行1秒以上（小于1秒计算rps会除0）
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来计算测试指标
        loadTestService.calculateTestMetrics(testName);
        //查询测试，确认当前job个数，设定的job总数，测试指标（RPS,平均响应时间，失败（非200）个数）
        LoadTestLargeScaleTask loadTestTask2 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask2.getJobAddedAmount() == 2;
        assert loadTestTask2.getJobAmount() == 2;
        TestMetrics testMetrics = loadTestService.getTestMetrics(testName);
        assert testMetrics.getRps() >= 18 && testMetrics.getRps() <= 22;
        assert testMetrics.getAvgLatency() >= 100 && testMetrics.getAvgLatency() <= 110;
        assert testMetrics.getTotalFailRequest() == 0;
        //停止测试
        loadTestService.stopTest(testName);
        //定时任务来停止Job
        jobExecuteService.stopJobsForStoppedTest(testName);
        //删除测试
        loadTestService.deleteTest(testName);


        //创建一个测试，指定要执行哪种Job,Job增加方式，Job总数（HelloWorld,每秒1个，3个）
        loadTestService.createGraduallyTest(testName, jobScriptName, 2, 1, 2000L,
                "", System.currentTimeMillis());
        //定时线程来给测试添加Job（先添加1个）
        loadTestService.addJobForTest(testName, System.currentTimeMillis());
        LoadTestLargeScaleTask loadTestTask3 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask3.getJobAddedAmount() == 1;
        assert !loadTestTask3.isAllJobAdded();
        //定时线程来获取一个Job，并执行
        LoadTestLargeScaleTaskSegment taskSegment3 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(taskSegment3);
        //让时间流逝1.1秒，确保Job执行1秒以上（小于1秒计算rps会除0）
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来计算测试指标
        loadTestService.calculateTestMetrics(testName);
        //查询测试，确认当前job个数，设定的job总数，测试指标（RPS,平均响应时间，失败（非200）个数）
        LoadTestLargeScaleTask loadTestTask4 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask4.getJobAddedAmount() == 1;
        assert loadTestTask4.getJobAmount() == 2;
        TestMetrics testMetrics2 = loadTestService.getTestMetrics(testName);
        assert testMetrics2.getRps() >= 9 && testMetrics2.getRps() <= 11;
        assert testMetrics2.getAvgLatency() >= 100 && testMetrics2.getAvgLatency() <= 110;
        assert testMetrics2.getTotalFailRequest() == 0;
        //定时线程来获取另一个Job，为空（还未生成）
        LoadTestLargeScaleTaskSegment taskSegment4 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        assert taskSegment4 == null;
        //定时线程来给测试添加Job（时间未到，不添加）
        long currTime = System.currentTimeMillis();
        loadTestService.addJobForTest(testName, currTime);
        LoadTestLargeScaleTask loadTestTask5 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask5.getJobAddedAmount() == 1;
        assert !loadTestTask5.isAllJobAdded();
        //时间流逝了1000ms
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来给测试添加Job（时间到，添加1个）
        loadTestService.addJobForTest(testName, System.currentTimeMillis());
        LoadTestLargeScaleTask loadTestTask6 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask6.getJobAddedAmount() == 2;
        assert loadTestTask6.isAllJobAdded();
        //定时线程来获取另一个Job，并执行
        LoadTestLargeScaleTaskSegment taskSegment5 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(taskSegment5);
        //让时间流逝1.1秒，确保Job执行1秒以上（小于1秒计算rps会除0）
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来计算测试指标
        loadTestService.calculateTestMetrics(testName);
        //查询测试，确认当前job个数，设定的job总数，测试指标（RPS,平均响应时间，失败（非200）个数）
        LoadTestLargeScaleTask loadTestTask7 = loadTestService.getLoadTestLargeScaleTask(testName);
        assert loadTestTask7.getJobAddedAmount() == 2;
        assert loadTestTask7.getJobAmount() == 2;
        TestMetrics testMetrics3 = loadTestService.getTestMetrics(testName);
        assert testMetrics3.getRps() >= 9 && testMetrics3.getRps() <= 24;
        assert testMetrics3.getAvgLatency() >= 100 && testMetrics3.getAvgLatency() <= 110;
        assert testMetrics3.getTotalFailRequest() == 0;
        //停止测试
        loadTestService.stopTest(testName);
        //定时任务来停止Job
        jobExecuteService.stopJobsForStoppedTest(testName);
        //删除测试
        loadTestService.deleteTest(testName);
    }
}

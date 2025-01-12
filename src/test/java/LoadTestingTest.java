import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.entity.LoadTestJob;
import com.loadtestingframework.entity.TestMetrics;
import com.loadtestingframework.repository.*;
import com.loadtestingframework.repository.impl.MemHttpExchangeRepository;
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
        LoadTestJobRepository loadTestJobRepository = RepositoryFactory.newInstance(LoadTestJobRepository.class,
                new RedisRepository(null, LoadTestJob.class));
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
        JobExecuteService jobExecuteService = new JobExecuteService();
        jobExecuteService.setJobExecuteStateRepository(jobExecuteStateRepository);
        jobExecuteService.setHttpExchangeIdGeneratorRepository(httpExchangeIdGeneratorRepository);
        jobExecuteService.setHttpExchangeRepository(httpExchangeRepository);
        jobExecuteService.setLoadTestRepository(loadTestRepository);

        //创建一个测试，指定要执行哪个Job脚本,Job增加方式，Job总数（HelloWorld,全量，2个）
        boolean jobScriptValid = jobExecuteService.isJobScriptValid(jobScriptName);
        assert jobScriptValid;
        loadTestService.createTest(testName, jobScriptName, 2, System.currentTimeMillis());
        //定时线程来给测试添加Job（全量2个）
        LoadTest loadTest1 = loadTestService.addJobForTest(testName, System.currentTimeMillis());
        assert loadTest1.isAllJobAdded();
        //定时线程来获取一个Job，并执行
        LoadTestJob loadTestJob1 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(loadTestJob1);
        //定时线程来获取另一个Job，并执行
        LoadTestJob loadTestJob2 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(loadTestJob2);
        //让时间流逝1.1秒，确保Job执行1秒以上（小于1秒计算rps会除0）
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来计算测试指标
        loadTestService.calculateTestMetrics(testName);
        //查询测试，确认当前job个数，设定的job总数，测试指标（RPS,平均响应时间，失败（非200）个数）
        LoadTest loadTest2 = loadTestService.getLoadTest(testName);
        assert loadTest2.getJobAddedAmount() == 2;
        assert loadTest2.getJobAmount() == 2;
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
                System.currentTimeMillis());
        //定时线程来给测试添加Job（先添加1个）
        LoadTest loadTest3 = loadTestService.addJobForTest(testName, System.currentTimeMillis());
        assert loadTest3.getJobAddedAmount() == 1;
        assert !loadTest3.isAllJobAdded();
        //定时线程来获取一个Job，并执行
        LoadTestJob loadTestJob3 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(loadTestJob3);
        //让时间流逝1.1秒，确保Job执行1秒以上（小于1秒计算rps会除0）
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来计算测试指标
        loadTestService.calculateTestMetrics(testName);
        //查询测试，确认当前job个数，设定的job总数，测试指标（RPS,平均响应时间，失败（非200）个数）
        LoadTest loadTest4 = loadTestService.getLoadTest(testName);
        assert loadTest4.getJobAddedAmount() == 1;
        assert loadTest4.getJobAmount() == 2;
        TestMetrics testMetrics2 = loadTestService.getTestMetrics(testName);
        assert testMetrics2.getRps() >= 9 && testMetrics2.getRps() <= 11;
        assert testMetrics2.getAvgLatency() >= 100 && testMetrics2.getAvgLatency() <= 110;
        assert testMetrics2.getTotalFailRequest() == 0;
        //定时线程来获取另一个Job，为空（还未生成）
        LoadTestJob loadTestJob4 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        assert loadTestJob4 == null;
        //定时线程来给测试添加Job（时间未到，不添加）
        long currTime = System.currentTimeMillis();
        LoadTest loadTest5 = loadTestService.addJobForTest(testName, currTime);
        assert loadTest5.getJobAddedAmount() == 1;
        assert !loadTest5.isAllJobAdded();
        //时间流逝了1000ms
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来给测试添加Job（时间到，添加1个）
        LoadTest loadTest6 = loadTestService.addJobForTest(testName, System.currentTimeMillis());
        assert loadTest6.getJobAddedAmount() == 2;
        assert loadTest6.isAllJobAdded();
        //定时线程来获取另一个Job，并执行
        LoadTestJob loadTestJob5 = loadTestService.takeJobToExecute(testName, System.currentTimeMillis());
        jobExecuteService.executeJob(loadTestJob5);
        //让时间流逝1.1秒，确保Job执行1秒以上（小于1秒计算rps会除0）
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //定时线程来计算测试指标
        loadTestService.calculateTestMetrics(testName);
        //查询测试，确认当前job个数，设定的job总数，测试指标（RPS,平均响应时间，失败（非200）个数）
        LoadTest loadTest7 = loadTestService.getLoadTest(testName);
        assert loadTest7.getJobAddedAmount() == 2;
        assert loadTest7.getJobAmount() == 2;
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

package com.loadtestingframework;

import com.loadtestingframework.entity.LoadTestFailures;
import com.loadtestingframework.entity.LoadTestLargeScaleTask;
import com.loadtestingframework.entity.LoadTestLargeScaleTaskSegment;
import com.loadtestingframework.entity.TestMetrics;
import com.loadtestingframework.repository.*;
import dml.id.entity.IdGenerator;
import dml.id.entity.SnowflakeIdGenerator;
import erp.ERP;
import erp.redis.ERPRedis;
import erp.redis.RedisRepository;
import erp.repository.factory.RepositoryFactory;
import erp.repository.factory.SingletonRepositoryFactory;
import erp.repository.impl.mem.MemSingletonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App {

    @Autowired
    private RedisTemplate redisTemplate;

    public static void main(String[] args) {
        ERP.useAnnotation();
        ERPRedis.usePipelineAnnotation();
        ApplicationContext context = SpringApplication.run(App.class, args);
    }

    @Bean
    public LoadTestJobIdGeneratorRepository loadTestJobIdGeneratorRepository() {
        return SingletonRepositoryFactory.newInstance(LoadTestJobIdGeneratorRepository.class,
                new MemSingletonRepository<IdGenerator<Long>>(new SnowflakeIdGenerator(1L),
                        "loadTestJobIdGeneratorRepository"));
    }

    @Bean
    public TestMetricsRepository testMetricsRepository() {
        return RepositoryFactory.newInstance(TestMetricsRepository.class,
                new RedisRepository(redisTemplate, TestMetrics.class));
    }

    @Bean
    public HttpExchangeIdGeneratorRepository httpExchangeIdGeneratorRepository() {
        return SingletonRepositoryFactory.newInstance(HttpExchangeIdGeneratorRepository.class,
                new MemSingletonRepository<IdGenerator<Long>>(new SnowflakeIdGenerator(1L),
                        "httpExchangeIdGeneratorRepository"));
    }

    @Bean
    public LoadTestLargeScaleTaskRepository loadTestLargeScaleTaskRepository() {
        return RepositoryFactory.newInstance(LoadTestLargeScaleTaskRepository.class,
                new RedisRepository(redisTemplate, LoadTestLargeScaleTask.class));
    }

    @Bean
    public LoadTestLargeScaleTaskSegmentRepository loadTestLargeScaleTaskSegmentRepository() {
        return RepositoryFactory.newInstance(LoadTestLargeScaleTaskSegmentRepository.class,
                new RedisRepository(redisTemplate, LoadTestLargeScaleTaskSegment.class));
    }

    @Bean
    public LoadTestFailuresRepository loadTestFailuresRepository() {
        return RepositoryFactory.newInstance(LoadTestFailuresRepository.class,
                new RedisRepository(redisTemplate, LoadTestFailures.class));
    }

}

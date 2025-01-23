package com.loadtestingframework.repository.impl;

import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.repository.LoadTestRepository;
import erp.redis.AllIdQuerySupportedRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisLoadTestRepository extends AllIdQuerySupportedRedisRepository<LoadTest, String> implements LoadTestRepository {

    @Autowired
    public RedisLoadTestRepository(RedisTemplate redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public List<String> getAllTestNames() {
        return queryAllIds();
    }
}

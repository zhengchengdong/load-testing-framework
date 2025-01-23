package com.loadtestingframework.repository.impl;

import com.loadtestingframework.entity.LoadTestJob;
import com.loadtestingframework.repository.LoadTestJobRepository;
import erp.redis.AllIdQuerySupportedRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RedisLoadTestJobRepository extends AllIdQuerySupportedRedisRepository<LoadTestJob, Long> implements LoadTestJobRepository {

    @Autowired
    public RedisLoadTestJobRepository(RedisTemplate redisTemplate) {
        super(redisTemplate);
    }


    @Override
    public List<Long> getAllIds() {
        List<String> allIdStrings = queryAllIds();
        return allIdStrings.stream().map(Long::parseLong).collect(Collectors.toList());
    }
}

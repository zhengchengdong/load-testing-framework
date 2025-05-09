package com.loadtestingframework.repository.impl;

import com.loadtestingframework.entity.HttpExchange;
import com.loadtestingframework.repository.FailureHttpExchangeRepository;
import erp.redis.AllIdQuerySupportedRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RedisFailureHttpExchangeRepository extends AllIdQuerySupportedRedisRepository<HttpExchange, Long> implements FailureHttpExchangeRepository {
    @Autowired
    public RedisFailureHttpExchangeRepository(RedisTemplate redisTemplate) {
        super(redisTemplate, "failureHttpExchange");
    }

    @Override
    public List<Long> getAllIds() {
        List<String> allStringId = queryAllIds();
        return allStringId.stream().map(Long::valueOf).collect(Collectors.toList());
    }
}

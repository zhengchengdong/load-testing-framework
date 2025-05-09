package com.loadtestingframework.repository;

import com.loadtestingframework.entity.HttpExchange;
import dml.common.repository.CommonRepository;

import java.util.List;

public interface FailureHttpExchangeRepository extends CommonRepository<HttpExchange, Long> {
    public List<Long> getAllIds();
}

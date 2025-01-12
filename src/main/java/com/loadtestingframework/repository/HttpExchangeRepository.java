package com.loadtestingframework.repository;

import com.loadtestingframework.entity.HttpExchange;
import dml.common.repository.CommonRepository;

import java.util.List;

public interface HttpExchangeRepository extends CommonRepository<HttpExchange, Long> {
    List<Long> getAllIds();
}

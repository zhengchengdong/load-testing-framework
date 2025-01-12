package com.loadtestingframework.repository.impl;

import com.loadtestingframework.entity.HttpExchange;
import com.loadtestingframework.repository.HttpExchangeRepository;
import erp.repository.impl.mem.MemRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemHttpExchangeRepository extends MemRepository<HttpExchange, Long> implements HttpExchangeRepository {
    public List<Long> getAllIds() {
        return queryAllIds();
    }
}

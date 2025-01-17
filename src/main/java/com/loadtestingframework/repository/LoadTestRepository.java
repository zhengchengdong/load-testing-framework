package com.loadtestingframework.repository;

import com.loadtestingframework.entity.LoadTest;
import dml.common.repository.CommonRepository;

import java.util.List;

public interface LoadTestRepository extends CommonRepository<LoadTest, String> {
    List<String> getAllTestNames();
}

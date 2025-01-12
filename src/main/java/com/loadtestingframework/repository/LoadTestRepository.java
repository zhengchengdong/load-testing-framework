package com.loadtestingframework.repository;

import com.loadtestingframework.entity.LoadTest;
import dml.largescaletaskmanagement.repository.LargeScaleTaskRepository;

import java.util.List;

public interface LoadTestRepository extends LargeScaleTaskRepository<LoadTest> {
    List<String> getAllTestNames();
}

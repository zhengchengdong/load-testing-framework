package com.loadtestingframework.repository;

import com.loadtestingframework.entity.LoadTestJob;
import dml.common.repository.CommonRepository;

import java.util.List;

public interface LoadTestJobRepository extends CommonRepository<LoadTestJob, Long> {
    List<Long> getAllIds();
}

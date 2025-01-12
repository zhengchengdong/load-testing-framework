package com.loadtestingframework.repository;

import com.loadtestingframework.entity.LoadTestJob;
import dml.largescaletaskmanagement.repository.LargeScaleTaskSegmentRepository;

public interface LoadTestJobRepository extends LargeScaleTaskSegmentRepository<LoadTestJob, Long> {
}

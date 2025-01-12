package com.loadtestingframework.repository;

import dml.common.repository.CommonSingletonRepository;
import dml.id.entity.IdGenerator;

public interface LoadTestJobIdGeneratorRepository extends CommonSingletonRepository<IdGenerator<Long>> {
}

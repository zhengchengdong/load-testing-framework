package com.loadtestingframework.repository;

import dml.common.repository.CommonSingletonRepository;
import dml.id.entity.IdGenerator;

public interface HttpExchangeIdGeneratorRepository extends CommonSingletonRepository<IdGenerator<Long>> {
}

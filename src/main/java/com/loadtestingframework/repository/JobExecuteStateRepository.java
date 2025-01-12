package com.loadtestingframework.repository;

import com.loadtestingframework.entity.JobExecuteState;
import erp.repository.impl.mem.MemRepository;
import org.springframework.stereotype.Component;

@Component
public class JobExecuteStateRepository extends MemRepository<JobExecuteState, Long> {
}

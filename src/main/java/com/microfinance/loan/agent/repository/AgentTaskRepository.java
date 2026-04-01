package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.AgentTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {
}

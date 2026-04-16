package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {
	long countByAgentId(Long agentId);

	long countByAgentIdAndTaskStatus(Long agentId, TaskStatus taskStatus);

	long countByAgentIdAndTaskStatusAndTaskType(Long agentId, TaskStatus taskStatus, AgentTaskType taskType);

	long countByAgentIdAndTaskStatusAndCompletedAtBetween(Long agentId,
														  TaskStatus taskStatus,
														  LocalDateTime start,
														  LocalDateTime end);
}

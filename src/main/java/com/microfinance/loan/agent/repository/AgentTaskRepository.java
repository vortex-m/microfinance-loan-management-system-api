package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {
	long countByAgentId(Long agentId);

	long countByAgentIdAndTaskStatus(Long agentId, TaskStatus taskStatus);

	long countByAgentIdAndTaskStatusAndTaskType(Long agentId, TaskStatus taskStatus, AgentTaskType taskType);

	long countByAgentIsNullAndTaskStatusAndTaskType(TaskStatus taskStatus, AgentTaskType taskType);

	long countByAgentIdAndTaskStatusAndCompletedAtBetween(Long agentId,
														  TaskStatus taskStatus,
														  LocalDateTime start,
														  LocalDateTime end);
	boolean existsByLoanApplicationIdAndAgentIdAndTaskTypeAndTaskStatusIn(Long loanApplicationId,
																   Long agentId,
																   AgentTaskType taskType,
																   List<TaskStatus> statuses);

	boolean existsByLoanApplicationIdAndTaskTypeAndTaskStatusIn(Long loanApplicationId,
										AgentTaskType taskType,
										List<TaskStatus> statuses);

	java.util.Optional<AgentTask> findByIdAndAgentId(Long taskId, Long agentId);

	List<AgentTask> findByAgentIdOrderByCreatedAtDesc(Long agentId);

	List<AgentTask> findByAgentIdAndTaskStatusOrderByCreatedAtDesc(Long agentId, TaskStatus taskStatus);

	List<AgentTask> findByAgentIdAndTaskTypeOrderByCreatedAtDesc(Long agentId, AgentTaskType taskType);

	List<AgentTask> findByAgentIsNullAndTaskStatusAndTaskTypeOrderByCreatedAtDesc(TaskStatus taskStatus,
															 AgentTaskType taskType);

	List<AgentTask> findByAgentIdAndTaskStatusAndTaskTypeOrderByCreatedAtDesc(Long agentId,
																TaskStatus taskStatus,
																AgentTaskType taskType);

	java.util.Optional<AgentTask> findTopByLoanApplicationIdAndAgentIdAndTaskTypeAndTaskStatusInOrderByCreatedAtDesc(
			Long loanApplicationId,
			Long agentId,
			AgentTaskType taskType,
			List<TaskStatus> statuses
	);

	java.util.Optional<AgentTask> findTopByLoanApplicationIdAndTaskTypeOrderByCreatedAtDesc(
			Long loanApplicationId,
			AgentTaskType taskType
	);

	java.util.Optional<AgentTask> findTopByLoanApplicationIdAndTaskTypeAndTaskStatusInOrderByCreatedAtDesc(
			Long loanApplicationId,
			AgentTaskType taskType,
			List<TaskStatus> statuses
	);

	List<AgentTask> findByTaskTypeAndTaskStatusInOrderByCreatedAtDesc(
			AgentTaskType taskType,
			List<TaskStatus> statuses
	);

}


package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.response.AgentDashboardResponse;
import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AgentDashboardService {

	private final CurrentUserService currentUserService;
	private final AgentProfileRepository agentProfileRepository;
	private final AgentTaskRepository agentTaskRepository;

	public AgentDashboardService(CurrentUserService currentUserService,
								 AgentProfileRepository agentProfileRepository,
								 AgentTaskRepository agentTaskRepository) {
		this.currentUserService = currentUserService;
		this.agentProfileRepository = agentProfileRepository;
		this.agentTaskRepository = agentTaskRepository;
	}

	public AgentDashboardResponse getDashboard(org.springframework.security.core.Authentication authentication) {
		Long agentUserId = currentUserService.getCurrentUserId(authentication);
		AgentProfile profile = agentProfileRepository.findByUsersId(agentUserId)
				.orElseThrow(() -> new IllegalArgumentException("Agent profile not found for user: " + agentUserId));

		LocalDateTime dayStart = LocalDate.now().atStartOfDay();
		LocalDateTime dayEnd = dayStart.plusDays(1);

		return AgentDashboardResponse.builder()
				.agentUserId(agentUserId)
				.agentCode(profile.getAgentCode())
				.totalAssignedTasks((int) agentTaskRepository.countByAgentId(agentUserId))
				.inProgressTasks((int) agentTaskRepository.countByAgentIdAndTaskStatus(agentUserId, TaskStatus.IN_PROGRESS))
				.pendingVerificationTasks((int) agentTaskRepository.countByAgentIdAndTaskStatusAndTaskType(agentUserId, TaskStatus.ASSIGNED, AgentTaskType.VERIFICATION))
				.pendingCashCollectionTasks((int) agentTaskRepository.countByAgentIdAndTaskStatusAndTaskType(agentUserId, TaskStatus.ASSIGNED, AgentTaskType.CASH_COLLECTION))
				.completedToday((int) agentTaskRepository.countByAgentIdAndTaskStatusAndCompletedAtBetween(agentUserId, TaskStatus.COMPLETED, dayStart, dayEnd))
				.build();
	}
}

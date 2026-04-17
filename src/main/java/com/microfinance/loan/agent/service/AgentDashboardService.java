package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.response.AgentDashboardResponse;
import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.CashSettlementStatus;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.payment.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AgentDashboardService {

	private final CurrentUserService currentUserService;
	private final AgentProfileRepository agentProfileRepository;
	private final AgentTaskRepository agentTaskRepository;
	private final TransactionRepository transactionRepository;

	public AgentDashboardService(CurrentUserService currentUserService,
								 AgentProfileRepository agentProfileRepository,
								 AgentTaskRepository agentTaskRepository,
								 TransactionRepository transactionRepository) {
		this.currentUserService = currentUserService;
		this.agentProfileRepository = agentProfileRepository;
		this.agentTaskRepository = agentTaskRepository;
		this.transactionRepository = transactionRepository;
	}

	public AgentDashboardResponse getDashboard(org.springframework.security.core.Authentication authentication) {
		Long agentUserId = currentUserService.getCurrentUserId(authentication);
		AgentProfile profile = agentProfileRepository.findByUsersId(agentUserId)
				.orElseThrow(() -> new IllegalArgumentException("Agent profile not found for user: " + agentUserId));

		LocalDateTime dayStart = LocalDate.now().atStartOfDay();
		LocalDateTime dayEnd = dayStart.plusDays(1);
		double unsettled = safeAmount(transactionRepository.sumCashAmountByAgentAndSettlementStatus(
				agentUserId, CashSettlementStatus.COLLECTED_UNSETTLED));
		double settled = safeAmount(transactionRepository.sumCashAmountByAgentAndSettlementStatus(
				agentUserId, CashSettlementStatus.SETTLED));

		return AgentDashboardResponse.builder()
				.agentUserId(agentUserId)
				.agentCode(profile.getAgentCode())
				.totalAssignedTasks((int) agentTaskRepository.countByAgentId(agentUserId))
				.assignedTasks((int) agentTaskRepository.countByAgentIdAndTaskStatus(agentUserId, TaskStatus.ASSIGNED))
				.acceptedTasks((int) agentTaskRepository.countByAgentIdAndTaskStatus(agentUserId, TaskStatus.ACCEPTED))
				.inProgressTasks((int) agentTaskRepository.countByAgentIdAndTaskStatus(agentUserId, TaskStatus.IN_PROGRESS))
				.pendingVerificationTasks((int) agentTaskRepository.countByAgentIdAndTaskStatusAndTaskType(agentUserId, TaskStatus.ASSIGNED, AgentTaskType.VERIFICATION))
				.pendingCashCollectionTasks((int) agentTaskRepository.countByAgentIdAndTaskStatusAndTaskType(agentUserId, TaskStatus.ASSIGNED, AgentTaskType.CASH_COLLECTION))
				.cashCollectionsInProgress((int) agentTaskRepository.countByAgentIdAndTaskStatusAndTaskType(agentUserId, TaskStatus.IN_PROGRESS, AgentTaskType.CASH_COLLECTION))
				.completedToday((int) agentTaskRepository.countByAgentIdAndTaskStatusAndCompletedAtBetween(agentUserId, TaskStatus.COMPLETED, dayStart, dayEnd))
				.agentAvailability(profile.getAgentAvailability())
				.lastLocationUpdatedAt(profile.getLastLocationUpdateAt())
				.totalCollectedCash(round(unsettled + settled))
				.totalSettledCash(round(settled))
				.totalUnsettledCash(round(unsettled))
				.build();
	}

	private double safeAmount(Double amount) {
		return amount == null ? 0d : amount;
	}

	private double round(double value) {
		return Math.round(value * 100d) / 100d;
	}
}

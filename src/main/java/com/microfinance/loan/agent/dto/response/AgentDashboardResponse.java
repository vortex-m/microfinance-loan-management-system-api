package com.microfinance.loan.agent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDashboardResponse {
	private Long agentUserId;
	private String agentCode;
	private Integer totalAssignedTasks;
	private Integer inProgressTasks;
	private Integer pendingVerificationTasks;
	private Integer pendingCashCollectionTasks;
	private Integer completedToday;
}

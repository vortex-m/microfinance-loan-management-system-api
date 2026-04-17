package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.AgentAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDashboardResponse {
	private Long agentUserId;
	private String agentCode;
	private Integer totalAssignedTasks;
	private Integer assignedTasks;
	private Integer acceptedTasks;
	private Integer inProgressTasks;
	private Integer pendingVerificationTasks;
	private Integer pendingCashCollectionTasks;
	private Integer cashCollectionsInProgress;
	private Integer completedToday;
	private AgentAvailability agentAvailability;
	private LocalDateTime lastLocationUpdatedAt;
	private Double totalCollectedCash;
	private Double totalSettledCash;
	private Double totalUnsettledCash;
}

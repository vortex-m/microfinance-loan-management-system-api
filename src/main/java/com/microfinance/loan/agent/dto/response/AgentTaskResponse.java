package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.TaskStatus;
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
public class AgentTaskResponse {
	private Long taskId;
	private String taskCode;
	private Long loanApplicationId;
	private String applicationNumber;

	private AgentTaskType taskType;
	private TaskStatus taskStatus;
	private String taskDescription;
	private String priorityLevel;

	private Boolean otpRequired;
	private Boolean otpVerified;
	private LocalDateTime otpRequestedAt;
	private LocalDateTime otpVerifiedAt;

	private LocalDateTime deadline;
	private LocalDateTime acceptedAt;
	private LocalDateTime startedAt;
	private LocalDateTime completedAt;
}

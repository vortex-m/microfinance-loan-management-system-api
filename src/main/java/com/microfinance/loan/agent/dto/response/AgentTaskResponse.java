package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;

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
	private Long userId;
	private String userName;
	private String userPhone;

	private Long loanId;
	private String loanNumber;
	private String loanStatus;
	private Double loanEmiAmount;
	private LocalDate nextEmiDueDate;
	private Long nextEmiScheduleId;
	private Double nextEmiOutstandingAmount;

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

	private LocalDateTime collectionPlannedAt;
	private LocalDateTime collectionStartedAt;
	private Double collectionStartedLat;
	private Double collectionStartedLng;
	private LocalDateTime collectionVerifiedAt;
	private Double collectionVerifiedLat;
	private Double collectionVerifiedLng;
}

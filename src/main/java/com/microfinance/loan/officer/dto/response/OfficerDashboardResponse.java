package com.microfinance.loan.officer.dto.response;

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
public class OfficerDashboardResponse {
	private Long officerUserId;
	private String officerCode;
	private String branchCode;
	private Long pendingKycDocuments;
	private Long pendingLoanQueue;
	private Long pendingNewLoanQueue;
	private Long pendingUnderReviewQueue;
	private Long pendingManagerApprovalQueue;
	private Long reviewsApproved;
	private Long reviewsRejected;
	private Long reviewsReverify;
	private Double totalCollectedCash;
	private Double totalSettledCash;
	private Double totalUnsettledCash;
}

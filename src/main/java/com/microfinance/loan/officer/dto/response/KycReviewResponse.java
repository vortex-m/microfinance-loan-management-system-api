package com.microfinance.loan.officer.dto.response;

import com.microfinance.loan.common.enums.KycStatus;
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
public class KycReviewResponse {
	private Long documentId;
	private Long userId;
	private String documentNumber;
	private KycStatus documentStatus;
	private KycStatus kycStatus;
	private String reviewRemarks;
	private String rejectionReason;
	private LocalDateTime reviewedAt;
}

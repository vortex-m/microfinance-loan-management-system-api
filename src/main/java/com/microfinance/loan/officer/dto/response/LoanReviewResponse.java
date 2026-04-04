package com.microfinance.loan.officer.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
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
public class LoanReviewResponse {
	private Long loanApplicationId;
	private String applicationNumber;
	private Long userId;
	private String userName;
	private Double requestedAmount;
	private Integer tenureMonths;
	private String loanPurpose;
	private DisbursalMode disbursalMode;
	private String disbursalBankName;
	private String disbursalBankAccount;
	private String disbursalIfscCode;
	private LoanStatus status;
	private LocalDateTime appliedAt;
}

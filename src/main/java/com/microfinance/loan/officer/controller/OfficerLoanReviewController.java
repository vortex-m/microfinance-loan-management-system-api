package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.LoanAgentAssignmentRequest;
import com.microfinance.loan.officer.dto.request.LoanDecisionRequest;
import com.microfinance.loan.officer.dto.response.LoanReviewResponse;
import com.microfinance.loan.officer.dto.response.OfficerUserProfileResponse;
import com.microfinance.loan.officer.dto.response.VerificationEvidenceResponse;
import com.microfinance.loan.officer.service.OfficerLoanReviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/officers/loans")
public class OfficerLoanReviewController {

	private final OfficerLoanReviewService officerLoanReviewService;

	public OfficerLoanReviewController(OfficerLoanReviewService officerLoanReviewService) {
		this.officerLoanReviewService = officerLoanReviewService;
	}

	@PreAuthorize("hasRole('OFFICER')")
	@GetMapping("/pending")
	public ApiResponse<List<LoanReviewResponse>> getPendingLoans(Authentication authentication) {
		return ApiResponse.success(
				"Pending loan queue fetched",
				officerLoanReviewService.getPendingLoanQueue(authentication)
		);
	}

	@PreAuthorize("hasRole('OFFICER')")
	@PostMapping("/{loanApplicationId}/decision")
	public ApiResponse<LoanReviewResponse> submitDecision(Authentication authentication,
														  @PathVariable Long loanApplicationId,
														  @Valid @RequestBody LoanDecisionRequest request) {
		return ApiResponse.success(
				"Loan decision submitted successfully",
				officerLoanReviewService.submitDecision(authentication, loanApplicationId, request)
		);
	}

	@PreAuthorize("hasRole('OFFICER')")
	@PostMapping("/{loanApplicationId}/assign-agent")
	public ApiResponse<LoanReviewResponse> assignAgent(Authentication authentication,
									 @PathVariable Long loanApplicationId,
									 @Valid @RequestBody LoanAgentAssignmentRequest request) {
		return ApiResponse.success(
				"Agent assigned successfully",
				officerLoanReviewService.assignAgent(authentication, loanApplicationId, request)
		);
	}

	@PreAuthorize("hasRole('OFFICER')")
	@GetMapping("/{loanApplicationId}/verification-evidence")
	public ApiResponse<VerificationEvidenceResponse> getVerificationEvidence(Authentication authentication,
												@PathVariable Long loanApplicationId) {
		return ApiResponse.success(
				"Verification evidence fetched successfully",
				officerLoanReviewService.getVerificationEvidence(authentication, loanApplicationId)
		);
	}

	@PreAuthorize("hasRole('OFFICER')")
	@GetMapping("/{loanApplicationId}/user-profile")
	public ApiResponse<OfficerUserProfileResponse> getUserProfile(Authentication authentication,
											 @PathVariable Long loanApplicationId) {
		return ApiResponse.success(
				"User profile fetched successfully",
				officerLoanReviewService.getUserProfile(authentication, loanApplicationId)
		);
	}

}

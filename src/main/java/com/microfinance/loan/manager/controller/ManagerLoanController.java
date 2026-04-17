package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.LoanAgentAssignmentRequest;
import com.microfinance.loan.manager.dto.request.ManagerLoanDecisionRequest;
import com.microfinance.loan.manager.dto.response.ManagerLoanResponse;
import com.microfinance.loan.manager.service.ManagerLoanService;
import com.microfinance.loan.officer.dto.response.VerificationEvidenceResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/loans")
public class ManagerLoanController {

	private final ManagerLoanService managerLoanService;

	public ManagerLoanController(ManagerLoanService managerLoanService) {
		this.managerLoanService = managerLoanService;
	}

	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping("/pending")
	public ApiResponse<List<ManagerLoanResponse>> getPendingManagerLoans(Authentication authentication) {
		return ApiResponse.success(
				"Pending manager approval loans fetched",
				managerLoanService.getManagerQueue(authentication)
		);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/{loanApplicationId}/decision")
	public ApiResponse<ManagerLoanResponse> decideLoan(Authentication authentication,
													   @PathVariable Long loanApplicationId,
													   @Valid @RequestBody ManagerLoanDecisionRequest request) {
		return ApiResponse.success(
				"Manager decision updated successfully",
				managerLoanService.decideLoan(authentication, loanApplicationId, request)
		);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/{loanApplicationId}/disbursal/bank")
	public ApiResponse<ManagerLoanResponse> confirmBankDisbursal(Authentication authentication,
																 @PathVariable Long loanApplicationId,
																 @RequestParam String transactionReference) {
		return ApiResponse.success(
				"Bank disbursal marked successfully",
				managerLoanService.confirmBankDisbursal(authentication, loanApplicationId, transactionReference)
		);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/{loanApplicationId}/assign-agent")
	public ApiResponse<ManagerLoanResponse> assignAgent(Authentication authentication,
									   @PathVariable Long loanApplicationId,
									   @Valid @RequestBody LoanAgentAssignmentRequest request) {
		return ApiResponse.success(
				"Agent assigned successfully",
				managerLoanService.assignAgent(authentication, loanApplicationId, request)
		);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/{loanApplicationId}/reassign-agent")
	public ApiResponse<ManagerLoanResponse> reassignAgent(Authentication authentication,
										 @PathVariable Long loanApplicationId,
										 @Valid @RequestBody LoanAgentAssignmentRequest request) {
		return ApiResponse.success(
				"Agent reassigned successfully",
				managerLoanService.reassignAgent(authentication, loanApplicationId, request)
		);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping("/{loanApplicationId}/verification-evidence")
	public ApiResponse<VerificationEvidenceResponse> getVerificationEvidence(Authentication authentication,
												@PathVariable Long loanApplicationId) {
		return ApiResponse.success(
				"Verification evidence fetched successfully",
				managerLoanService.getVerificationEvidence(authentication, loanApplicationId)
		);
	}
}


package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.request.EmiPayRequest;
import com.microfinance.loan.user.dto.response.EmiScheduleResponse;
import com.microfinance.loan.user.dto.response.PaymentHistoryResponse;
import com.microfinance.loan.user.service.UserPaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/loans")
public class UserLoanPaymentController {

	private final UserPaymentService userPaymentService;
	private final CurrentUserService currentUserService;

	public UserLoanPaymentController(UserPaymentService userPaymentService,
									 CurrentUserService currentUserService) {
		this.userPaymentService = userPaymentService;
		this.currentUserService = currentUserService;
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/{loanId}/emi-schedule")
	public ApiResponse<EmiScheduleResponse> getEmiSchedule(Authentication authentication,
													   @PathVariable Long loanId) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("Loan EMI schedule fetched successfully", userPaymentService.getEmiSchedule(userId, loanId));
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/{loanId}/emi/pay")
	public ApiResponse<PaymentHistoryResponse.PaymentItem> payEmi(Authentication authentication,
														 @PathVariable Long loanId,
														 @Valid @RequestBody EmiPayRequest request) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("EMI payment processed successfully", userPaymentService.payEmiForLoan(userId, loanId, request));
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/{loanId}/payments")
	public ApiResponse<PaymentHistoryResponse> getLoanPayments(Authentication authentication,
													   @PathVariable Long loanId) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("Loan payment history fetched successfully", userPaymentService.getPaymentHistoryByLoan(userId, loanId));
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/payments")
	public ApiResponse<PaymentHistoryResponse> getAllPayments(Authentication authentication) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("Payment history fetched successfully", userPaymentService.getPaymentHistory(userId));
	}
}


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
@RequestMapping("/users/payments")
public class UserPaymentController {

	private final UserPaymentService userPaymentService;
	private final CurrentUserService currentUserService;

	public UserPaymentController(UserPaymentService userPaymentService,
								 CurrentUserService currentUserService) {
		this.userPaymentService = userPaymentService;
		this.currentUserService = currentUserService;
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/emi/pay")
	public ApiResponse<PaymentHistoryResponse.PaymentItem> payEmi(Authentication authentication,
																  @Valid @RequestBody EmiPayRequest request) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success(
				"EMI payment processed successfully",
				userPaymentService.payEmi(userId, request)
		);
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/loans/{loanId}/schedule")
	public ApiResponse<EmiScheduleResponse> getSchedule(Authentication authentication,
														@PathVariable Long loanId) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success(
				"EMI schedule fetched successfully",
				userPaymentService.getEmiSchedule(userId, loanId)
		);
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/history")
	public ApiResponse<PaymentHistoryResponse> getHistory(Authentication authentication) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success(
				"Payment history fetched successfully",
				userPaymentService.getPaymentHistory(userId)
		);
	}
}

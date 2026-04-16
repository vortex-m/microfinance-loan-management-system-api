package com.microfinance.loan.loan.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.response.LoanDetailResponse;
import com.microfinance.loan.user.dto.response.LoanStatusResponse;
import com.microfinance.loan.user.service.UserLoanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loans")
public class LoanController {

	private final UserLoanService userLoanService;
	private final CurrentUserService currentUserService;

	public LoanController(UserLoanService userLoanService,
						  CurrentUserService currentUserService) {
		this.userLoanService = userLoanService;
		this.currentUserService = currentUserService;
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/my/status")
	public ApiResponse<LoanStatusResponse> getMyStatuses(Authentication authentication) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("User loan statuses fetched successfully", userLoanService.getMyLoanStatuses(userId));
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/my/{loanApplicationId}")
	public ApiResponse<LoanDetailResponse> getMyLoanDetail(Authentication authentication,
											   @PathVariable Long loanApplicationId) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("User loan detail fetched successfully", userLoanService.getMyLoanDetail(userId, loanApplicationId));
	}
}


package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.response.UserDashboardResponse;
import com.microfinance.loan.user.service.UserDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/dashboard")
public class UserDashboardController {

	private final UserDashboardService userDashboardService;
	private final CurrentUserService currentUserService;

	public UserDashboardController(UserDashboardService userDashboardService,
								   CurrentUserService currentUserService) {
		this.userDashboardService = userDashboardService;
		this.currentUserService = currentUserService;
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping
	public ApiResponse<UserDashboardResponse> getDashboard(Authentication authentication) {
		Long userId = currentUserService.getCurrentUserId(authentication);
		return ApiResponse.success("User dashboard fetched successfully", userDashboardService.getDashboard(userId));
	}
}

package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.officer.dto.response.OfficerDashboardResponse;
import com.microfinance.loan.officer.service.OfficerDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/officers/dashboard")
public class OfficerDashboardController {

	private final OfficerDashboardService officerDashboardService;

	public OfficerDashboardController(OfficerDashboardService officerDashboardService) {
		this.officerDashboardService = officerDashboardService;
	}

	@PreAuthorize("hasRole('OFFICER')")
	@GetMapping
	public ApiResponse<OfficerDashboardResponse> getDashboard(Authentication authentication) {
		return ApiResponse.success("Officer dashboard fetched successfully", officerDashboardService.getDashboard(authentication));
	}
}

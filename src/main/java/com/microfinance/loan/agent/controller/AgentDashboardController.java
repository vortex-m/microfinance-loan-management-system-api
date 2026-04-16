package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.response.AgentDashboardResponse;
import com.microfinance.loan.agent.service.AgentDashboardService;
import com.microfinance.loan.common.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents/dashboard")
public class AgentDashboardController {

	private final AgentDashboardService agentDashboardService;

	public AgentDashboardController(AgentDashboardService agentDashboardService) {
		this.agentDashboardService = agentDashboardService;
	}

	@PreAuthorize("hasRole('AGENT')")
	@GetMapping
	public ApiResponse<AgentDashboardResponse> getDashboard(Authentication authentication) {
		return ApiResponse.success("Agent dashboard fetched successfully", agentDashboardService.getDashboard(authentication));
	}
}

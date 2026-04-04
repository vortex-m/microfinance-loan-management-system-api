package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.AgentProfileUpdateRequest;
import com.microfinance.loan.agent.dto.response.AgentProfileResponse;
import com.microfinance.loan.agent.service.AgentProfileService;
import com.microfinance.loan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// own profile + availability + location
@RestController
@RequestMapping("/agents")
public class AgentProfileController {

	private final AgentProfileService agentProfileService;

	public AgentProfileController(AgentProfileService agentProfileService) {
		this.agentProfileService = agentProfileService;
	}

	@PreAuthorize("hasRole('AGENT') and @userAccessGuard.canAccessUser(#userId, authentication)")
	@PutMapping("/{userId}/onboarding")
	public ApiResponse<AgentProfileResponse> completeOnboarding(
			@PathVariable Long userId,
			@Valid @RequestBody AgentProfileUpdateRequest request
	) {
		AgentProfileResponse response = agentProfileService.completeOnboarding(userId, request);
		String message = Boolean.TRUE.equals(response.getIsHome())
				? "Agent onboarding completed successfully"
				: "Agent onboarding saved, pending required details";
		return ApiResponse.success(message, response);
	}
}

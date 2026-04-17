package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.AgentProfileUpdateRequest;
import com.microfinance.loan.agent.dto.request.AgentAvailabilityUpdateRequest;
import com.microfinance.loan.agent.dto.request.AgentLocationUpdateRequest;
import com.microfinance.loan.agent.dto.response.AgentProfileResponse;
import com.microfinance.loan.agent.service.AgentProfileService;
import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// own profile + availability + location
@RestController
@RequestMapping("/agents")
public class AgentProfileController {

	private final AgentProfileService agentProfileService;
	private final CurrentUserService currentUserService;

	public AgentProfileController(AgentProfileService agentProfileService, CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
		this.agentProfileService = agentProfileService;
	}

	@PreAuthorize("hasRole('AGENT')")
	@GetMapping("/profile")
	public ApiResponse<AgentProfileResponse> getMyProfile(Authentication auth) {
		Long userId = currentUserService.getCurrentUserId(auth);
		return ApiResponse.success("Agent profile fetched successfully", agentProfileService.getMyProfile(userId));
	}

	@PreAuthorize("hasRole('AGENT')")
	@PutMapping("/onboarding")
	public ApiResponse<AgentProfileResponse> completeOnboarding(
			Authentication auth,
			@Valid @RequestBody AgentProfileUpdateRequest request
	) {
		Long userId = currentUserService.getCurrentUserId(auth);

		AgentProfileResponse response = agentProfileService.completeOnboarding(userId, request);
		String message = Boolean.TRUE.equals(response.getIsHome())
				? "Agent onboarding completed successfully"
				: "Agent onboarding saved, pending required details";
		return ApiResponse.success(message, response);
	}

	@PreAuthorize("hasRole('AGENT')")
	@PatchMapping("/availability")
	public ApiResponse<AgentProfileResponse> updateAvailability(Authentication auth,
												 @Valid @RequestBody AgentAvailabilityUpdateRequest request) {
		Long userId = currentUserService.getCurrentUserId(auth);
		return ApiResponse.success("Availability updated successfully",
				agentProfileService.updateAvailability(userId, request.getAvailability()));
	}

	@PreAuthorize("hasRole('AGENT')")
	@PatchMapping("/location")
	public ApiResponse<AgentProfileResponse> updateLocation(Authentication auth,
										 @Valid @RequestBody AgentLocationUpdateRequest request) {
		Long userId = currentUserService.getCurrentUserId(auth);
		return ApiResponse.success("Location updated successfully",
				agentProfileService.updateLocation(userId, request.getLatitude(), request.getLongitude()));
	}
}

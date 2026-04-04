package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.officer.dto.request.OfficerProfileUpdateRequest;
import com.microfinance.loan.officer.dto.response.OfficerProfileResponse;
import com.microfinance.loan.officer.service.OfficerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/officers")
public class OfficerProfileController {

	private final OfficerProfileService officerProfileService;

	public OfficerProfileController(OfficerProfileService officerProfileService) {
		this.officerProfileService = officerProfileService;
	}

	@PreAuthorize("hasRole('OFFICER') and @userAccessGuard.canAccessUser(#userId, authentication)")
	@PutMapping("/{userId}/onboarding")
	public ApiResponse<OfficerProfileResponse> completeOnboarding(
			@PathVariable Long userId,
			@Valid @RequestBody OfficerProfileUpdateRequest request
	) {
		OfficerProfileResponse response = officerProfileService.completeOnboarding(userId, request);
		String message = Boolean.TRUE.equals(response.getIsHome())
				? "Officer onboarding completed successfully"
				: "Officer onboarding saved, pending required details";
		return ApiResponse.success(message, response);
	}
}

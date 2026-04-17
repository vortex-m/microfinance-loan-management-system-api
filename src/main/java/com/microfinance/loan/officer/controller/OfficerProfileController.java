package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.officer.dto.request.OfficerProfileUpdateRequest;
import com.microfinance.loan.officer.dto.response.OfficerProfileResponse;
import com.microfinance.loan.officer.service.OfficerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/officers")
public class OfficerProfileController {

	private final OfficerProfileService officerProfileService;
	private final CurrentUserService currentUserService;

	public OfficerProfileController(OfficerProfileService officerProfileService, CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
		this.officerProfileService = officerProfileService;
	}

	@PreAuthorize("hasRole('OFFICER')")
	@PutMapping("/onboarding")
	public ApiResponse<OfficerProfileResponse> completeOnboarding(
			Authentication auth,
			@Valid @RequestBody OfficerProfileUpdateRequest request
	) {
		Long userId = currentUserService.getCurrentUserId(auth);

		OfficerProfileResponse response = officerProfileService.completeOnboarding(userId, request);
		String message = Boolean.TRUE.equals(response.getIsHome())
				? "Officer onboarding completed successfully"
				: "Officer onboarding saved, pending required details";
		return ApiResponse.success(message, response);
	}

	@PreAuthorize("hasRole('OFFICER')")
	@GetMapping("/profile")
	public ApiResponse<OfficerProfileResponse> getMyProfile(Authentication auth) {
		Long userId = currentUserService.getCurrentUserId(auth);
		OfficerProfileResponse response = officerProfileService.getOfficerProfile(userId);
		return ApiResponse.success("Officer profile fetched successfully", response);
	}

}

package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.user.dto.request.UpdateProfileRequest;
import com.microfinance.loan.user.dto.response.UserProfileResponse;
import com.microfinance.loan.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PreAuthorize("hasRole('USER') and @userAccessGuard.canAccessUser(#userId, authentication)")
    @PutMapping("/{userId}/onboarding")
    public ApiResponse<UserProfileResponse> completeOnboarding(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse response = userProfileService.completeOnboarding(userId, request);
        String message = Boolean.TRUE.equals(response.getIsHome())
                ? "Onboarding completed successfully"
                : "Onboarding saved, pending required details";

        return ApiResponse.success(message, response);
    }
}

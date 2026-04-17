package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.request.UpdateProfileRequest;
import com.microfinance.loan.user.dto.response.UserProfileResponse;
import com.microfinance.loan.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final CurrentUserService currentUserService;

    public UserProfileController(UserProfileService userProfileService, CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/onboarding")
    public ApiResponse<UserProfileResponse> completeOnboarding(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long userId = currentUserService.getCurrentUserId(auth);

        UserProfileResponse response = userProfileService.completeOnboarding(userId, request);
        String message = Boolean.TRUE.equals(response.getIsHome())
                ? "Onboarding completed successfully"
                : "Onboarding saved, pending required details";

        return ApiResponse.success(message, response);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getMyProfile(Authentication auth) {
        Long userId = currentUserService.getCurrentUserId(auth);
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ApiResponse.success("User profile fetched successfully", response);
    }
}

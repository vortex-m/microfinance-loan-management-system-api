package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.response.ManagerUserResponse;
import com.microfinance.loan.user.service.UserProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/users")
public class ManagerUserController {

    private final UserProfileService userProfileService;

    public ManagerUserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/getAll")
    public ApiResponse<List<ManagerUserResponse>> getAll(){
        return ApiResponse.success("Users fetched successfully", userProfileService.getAllUserProfilesForManager());
    }
}


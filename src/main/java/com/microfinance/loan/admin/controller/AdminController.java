package com.microfinance.loan.admin.controller;

import com.microfinance.loan.admin.dto.request.AdminProfileUpsertRequest;
import com.microfinance.loan.admin.dto.request.CreateAdminRequest;
import com.microfinance.loan.admin.dto.response.AdminProfileResponse;
import com.microfinance.loan.admin.dto.request.CreateBranchOperationManagerRequest;
import com.microfinance.loan.admin.dto.response.BranchMetricsResponse;
import com.microfinance.loan.admin.dto.response.CreateAdminResponse;
import com.microfinance.loan.admin.dto.response.CreateBranchOperationManagerResponse;
import com.microfinance.loan.admin.service.AdminService;
import com.microfinance.loan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ApiResponse<CreateAdminResponse> createAdmin(@Valid @RequestBody CreateAdminRequest createAdminRequest) {
        return ApiResponse.success("Admin Created Successfully.", adminService.createAdmin(createAdminRequest));
    }

    @PostMapping("/bootstrap")
    public ApiResponse<CreateAdminResponse> bootstrapAdmin(@Valid @RequestBody CreateAdminRequest createAdminRequest) {
        return ApiResponse.success("Bootstrap Admin Successfully.", adminService.bootstrapAdmin(createAdminRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile")
    public ApiResponse<AdminProfileResponse> getProfile(Authentication authentication) {
        return ApiResponse.success("Admin profile fetched successfully", adminService.getMyProfile(authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/profile")
    public ApiResponse<AdminProfileResponse> upsertProfile(
            Authentication authentication,
            @Valid @RequestBody AdminProfileUpsertRequest request
    ) {
        return ApiResponse.success("Admin profile saved successfully", adminService.upsertMyProfile(authentication, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/managers/branch-operations")
    public ApiResponse<CreateBranchOperationManagerResponse> createBranchOperationsManager(
            @Valid @RequestBody CreateBranchOperationManagerRequest request
    ) {
        return ApiResponse.success(
                "Branch operations manager created successfully",
                adminService.createBranchOperationManager(request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/branches/{branchCode}/metrics")
    public ApiResponse<BranchMetricsResponse> getBranchMetrics(@PathVariable String branchCode) {
        return ApiResponse.success(
                "Branch metrics fetched successfully",
                adminService.getBranchMetrics(branchCode)
        );
    }
}




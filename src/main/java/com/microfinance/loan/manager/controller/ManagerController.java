package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.request.ManagerProfileUpdateRequest;
import com.microfinance.loan.manager.dto.response.DashboardResponse;
import com.microfinance.loan.manager.dto.response.ManagerProfileResponse;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import com.microfinance.loan.manager.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager")
public class ManagerController {

	private final ManagerService managerService;

	public ManagerController(ManagerService managerService) {
		this.managerService = managerService;
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/staff/create/agent")
	public ApiResponse<StaffCreateResponse> createAgent(
			Authentication authentication,
			@Valid @RequestBody CreateStaffRequest request
	) {
		StaffCreateResponse response = managerService.createAgent(authentication, request);
		return ApiResponse.success("Agent created successfully", response);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/staff/create/manager")
	public ApiResponse<StaffCreateResponse> createManager(
			Authentication authentication,
			@Valid @RequestBody CreateStaffRequest request
	) {
		StaffCreateResponse response = managerService.createManager(authentication, request);
		return ApiResponse.success("Manager created successfully", response);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/staff/create/officer")
	public ApiResponse<StaffCreateResponse> createOfficer(
			Authentication authentication,
			@Valid @RequestBody CreateStaffRequest request
	) {
		StaffCreateResponse response = managerService.createOfficer(authentication, request);
		return ApiResponse.success("Officer created successfully", response);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PutMapping("/profile")
	public ApiResponse<ManagerProfileResponse> updateProfile(
			Authentication authentication,
			@Valid @RequestBody ManagerProfileUpdateRequest request
	) {
		return ApiResponse.success("Manager profile updated successfully", managerService.updateProfile(authentication, request));
	}

	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping("/dashboard")
	public ApiResponse<DashboardResponse> getDashboard(Authentication authentication) {
		return ApiResponse.success("Manager dashboard fetched successfully", managerService.getDashboard(authentication));
	}

}

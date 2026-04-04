package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import com.microfinance.loan.manager.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
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
	public ApiResponse<StaffCreateResponse> createAgent(@Valid @RequestBody CreateStaffRequest request) {
		StaffCreateResponse response = managerService.createAgent(request);
		return ApiResponse.success("Agent created successfully", response);
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/staff/create/officer")
	public ApiResponse<StaffCreateResponse> createOfficer(@Valid @RequestBody CreateStaffRequest request) {
		StaffCreateResponse response = managerService.createOfficer(request);
		return ApiResponse.success("Officer created successfully", response);
	}

}

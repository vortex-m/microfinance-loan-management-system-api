package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.GenerateSystemReportRequest;
import com.microfinance.loan.manager.dto.response.SystemReportResponse;
import com.microfinance.loan.manager.service.SystemReportService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/reports")
public class ManagerReportController {

	private final SystemReportService systemReportService;

	public ManagerReportController(SystemReportService systemReportService) {
		this.systemReportService = systemReportService;
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/generate")
	public ApiResponse<SystemReportResponse> generate(Authentication authentication,
													  @Valid @RequestBody GenerateSystemReportRequest request) {
		return ApiResponse.success("System report generated successfully", systemReportService.generate(authentication, request));
	}

	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping
	public ApiResponse<List<SystemReportResponse>> getMyReports(Authentication authentication) {
		return ApiResponse.success("System reports fetched", systemReportService.getMyBranchReports(authentication));
	}

	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping("/{reportId}")
	public ApiResponse<SystemReportResponse> getById(Authentication authentication,
													 @PathVariable Long reportId) {
		return ApiResponse.success("System report fetched", systemReportService.getById(authentication, reportId));
	}
}


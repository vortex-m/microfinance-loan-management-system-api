package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.FraudAlertActionRequest;
import com.microfinance.loan.manager.dto.response.FraudAlertResponse;
import com.microfinance.loan.manager.service.ManagerFraudService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/fraud-alerts")
public class ManagerFraudController {

	private final ManagerFraudService managerFraudService;

	public ManagerFraudController(ManagerFraudService managerFraudService) {
		this.managerFraudService = managerFraudService;
	}

	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping
	public ApiResponse<List<FraudAlertResponse>> getAlerts(Authentication authentication,
														   @RequestParam(required = false) String status) {
		return ApiResponse.success("Fraud alerts fetched", managerFraudService.getAlerts(authentication, status));
	}

	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/{alertId}/action")
	public ApiResponse<FraudAlertResponse> applyAction(Authentication authentication,
													   @PathVariable Long alertId,
													   @Valid @RequestBody FraudAlertActionRequest request) {
		return ApiResponse.success("Fraud alert action applied", managerFraudService.applyAction(authentication, alertId, request));
	}
}


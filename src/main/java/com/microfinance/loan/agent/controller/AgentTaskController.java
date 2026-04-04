package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.CashDisbursalOtpGenerateRequest;
import com.microfinance.loan.agent.dto.request.CashDisbursalOtpVerifyRequest;
import com.microfinance.loan.agent.dto.response.CashDisbursalOtpResponse;
import com.microfinance.loan.agent.service.AgentTaskService;
import com.microfinance.loan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents")
public class AgentTaskController {

    private final AgentTaskService agentTaskService;

    public AgentTaskController(AgentTaskService agentTaskService) {
        this.agentTaskService = agentTaskService;
    }

    @PreAuthorize("hasRole('AGENT') and @userAccessGuard.canAccessUser(#agentId, authentication)")
    @PostMapping("/{agentId}/cash-disbursal/otp/generate")
    public ApiResponse<CashDisbursalOtpResponse> generateCashDisbursalOtp(
            @PathVariable Long agentId,
            @Valid @RequestBody CashDisbursalOtpGenerateRequest request
    ) {
        return ApiResponse.success("Cash disbursal OTP generated", agentTaskService.generateCashDisbursalOtp(agentId, request));
    }

    @PreAuthorize("hasRole('AGENT') and @userAccessGuard.canAccessUser(#agentId, authentication)")
    @PostMapping("/{agentId}/cash-disbursal/otp/verify")
    public ApiResponse<CashDisbursalOtpResponse> verifyCashDisbursalOtp(
            @PathVariable Long agentId,
            @Valid @RequestBody CashDisbursalOtpVerifyRequest request
    ) {
        return ApiResponse.success("Cash disbursal OTP verified", agentTaskService.verifyCashDisbursalOtp(agentId, request));
    }
}

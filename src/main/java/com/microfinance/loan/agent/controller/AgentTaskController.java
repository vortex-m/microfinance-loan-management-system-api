package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.CashDisbursalOtpGenerateRequest;
import com.microfinance.loan.agent.dto.request.CashDisbursalOtpVerifyRequest;
import com.microfinance.loan.agent.dto.request.CashOtpRequest;
import com.microfinance.loan.agent.dto.request.CashOtpVerifyRequest;
import com.microfinance.loan.agent.dto.response.CashDisbursalOtpResponse;
import com.microfinance.loan.agent.dto.response.CashOtpResponse;
import com.microfinance.loan.agent.service.AgentTaskService;
import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents")
public class AgentTaskController {

    private final AgentTaskService agentTaskService;
    private final CurrentUserService currentUserService;

    public AgentTaskController(AgentTaskService agentTaskService, CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
        this.agentTaskService = agentTaskService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/cash-disbursal/otp/generate")
    public ApiResponse<CashDisbursalOtpResponse> generateCashDisbursalOtp(
            Authentication  auth,
            @Valid @RequestBody CashDisbursalOtpGenerateRequest request
    ) {
        Long agentId = currentUserService.getCurrentUserId(auth);

        return ApiResponse.success("Cash disbursal OTP generated", agentTaskService.generateCashDisbursalOtp(agentId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/cash-disbursal/otp/verify")
    public ApiResponse<CashDisbursalOtpResponse> verifyCashDisbursalOtp(
            Authentication  auth,
            @Valid @RequestBody CashDisbursalOtpVerifyRequest request
    ) {
        Long agentId = currentUserService.getCurrentUserId(auth);

        return ApiResponse.success("Cash disbursal OTP verified", agentTaskService.verifyCashDisbursalOtp(agentId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/cash-collection/otp/generate")
    public ApiResponse<CashOtpResponse> generateCollectionOtp(
            Authentication auth,
            @Valid @RequestBody CashOtpRequest request
    ) {
        Long agentId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Collection OTP generated", agentTaskService.generateCollectionOtp(agentId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/cash-collection/otp/verify")
    public ApiResponse<CashOtpResponse> verifyCollectionOtp(
            Authentication auth,
            @Valid @RequestBody CashOtpVerifyRequest request
    ) {
        Long agentId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Collection OTP verified", agentTaskService.verifyCollectionOtp(agentId, request));
    }
}

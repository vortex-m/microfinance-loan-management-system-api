package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.AgentLoanApplyForUserRequest;
import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.service.UserLoanService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents/loans")
public class AgentLoanController {

    private final UserLoanService userLoanService;
    private final CurrentUserService currentUserService;

    public AgentLoanController(UserLoanService userLoanService,
                               CurrentUserService currentUserService) {
        this.userLoanService = userLoanService;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/apply-for-user")
    public ApiResponse<LoanApplyResponse> applyForUser(Authentication authentication,
                                                       @Valid @RequestBody AgentLoanApplyForUserRequest request) {
        Long agentUserId = currentUserService.getCurrentUserId(authentication);
        return ApiResponse.success("Loan application submitted by agent successfully",
                userLoanService.applyForLoanByAgent(agentUserId, request));
    }
}


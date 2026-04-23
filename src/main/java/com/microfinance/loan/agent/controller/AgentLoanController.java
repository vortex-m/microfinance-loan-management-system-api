package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.AgentLoanApplyForUserRequest;
import com.microfinance.loan.agent.dto.response.AgentAssignedLoanResponse;
import com.microfinance.loan.agent.dto.response.AgentEmiScheduleResponse;
import com.microfinance.loan.agent.service.AgentLoanReadService;
import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.service.UserLoanService;
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
@RequestMapping("/agents/loans")
public class AgentLoanController {

    private final UserLoanService userLoanService;
    private final AgentLoanReadService agentLoanReadService;
    private final CurrentUserService currentUserService;

    public AgentLoanController(UserLoanService userLoanService,
                               AgentLoanReadService agentLoanReadService,
                               CurrentUserService currentUserService) {
        this.userLoanService = userLoanService;
        this.agentLoanReadService = agentLoanReadService;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/assigned-users")
    public ApiResponse<List<AgentAssignedLoanResponse>> getAssignedUsersAndLoans(Authentication authentication) {
        Long agentUserId = currentUserService.getCurrentUserId(authentication);
        return ApiResponse.success("Assigned user and loan data fetched successfully",
                agentLoanReadService.listAssignedApplications(agentUserId));
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/{loanId}/emis")
    public ApiResponse<List<AgentEmiScheduleResponse>> getLoanEmis(Authentication authentication,
                                                                   @PathVariable Long loanId) {
        Long agentUserId = currentUserService.getCurrentUserId(authentication);
        return ApiResponse.success("Loan EMI schedule fetched successfully",
                agentLoanReadService.listLoanEmis(agentUserId, loanId));
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


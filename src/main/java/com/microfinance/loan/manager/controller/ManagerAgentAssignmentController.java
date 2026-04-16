package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.request.BulkAgentReassignRequest;
import com.microfinance.loan.manager.dto.response.BulkAgentReassignResponse;
import com.microfinance.loan.manager.service.ManagerLoanService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/agents")
public class ManagerAgentAssignmentController {

    private final ManagerLoanService managerLoanService;

    public ManagerAgentAssignmentController(ManagerLoanService managerLoanService) {
        this.managerLoanService = managerLoanService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/reassign-bulk")
    public ApiResponse<BulkAgentReassignResponse> reassignBulk(Authentication authentication,
                                                               @Valid @RequestBody BulkAgentReassignRequest request) {
        return ApiResponse.success("Bulk agent reassignment completed",
                managerLoanService.reassignBulk(authentication, request));
    }
}


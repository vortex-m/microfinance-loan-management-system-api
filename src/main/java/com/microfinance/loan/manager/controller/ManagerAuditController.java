package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.lead.dto.response.AssistedAuditResponse;
import com.microfinance.loan.lead.service.AssistedAuditQueryService;
import com.microfinance.loan.manager.dto.request.AuditFlagRequest;
import com.microfinance.loan.manager.dto.response.AuditLogResponse;
import com.microfinance.loan.manager.service.ManagerAuditService;
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
@RequestMapping("/manager/audit")
public class ManagerAuditController {

    private final AssistedAuditQueryService assistedAuditQueryService;
    private final ManagerAuditService managerAuditService;

    public ManagerAuditController(AssistedAuditQueryService assistedAuditQueryService,
                                  ManagerAuditService managerAuditService) {
        this.assistedAuditQueryService = assistedAuditQueryService;
        this.managerAuditService = managerAuditService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/assisted-actions")
    public ApiResponse<List<AssistedAuditResponse>> getLatestAssistedActions(Authentication authentication) {
        return ApiResponse.success("Assisted action audit logs fetched", assistedAuditQueryService.getLatest(authentication));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/loan/{loanApplicationId}")
    public ApiResponse<List<AuditLogResponse>> getLoanAuditLogs(Authentication authentication,
                                                                @PathVariable Long loanApplicationId) {
        return ApiResponse.success("Loan audit logs fetched", managerAuditService.getLoanAuditLogs(authentication, loanApplicationId));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{auditId}/flag")
    public ApiResponse<AuditLogResponse> flagAudit(Authentication authentication,
                                                   @PathVariable Long auditId,
                                                   @Valid @RequestBody AuditFlagRequest request) {
        return ApiResponse.success("Audit flag updated", managerAuditService.flagAuditLog(authentication, auditId, request));
    }
}

package com.microfinance.loan.manager.service;

import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.manager.dto.request.AuditFlagRequest;
import com.microfinance.loan.manager.dto.response.AuditLogResponse;
import com.microfinance.loan.manager.entity.AuditLog;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ManagerAuditService {

    private final AuditLogRepository auditLogRepository;
    private final ManagerAuthorizationService managerAuthorizationService;

    public ManagerAuditService(AuditLogRepository auditLogRepository,
                               ManagerAuthorizationService managerAuthorizationService) {
        this.auditLogRepository = auditLogRepository;
        this.managerAuthorizationService = managerAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLoanAuditLogs(Authentication authentication, Long loanApplicationId) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        managerAuthorizationService.requireDepartment(managerProfile,
                ManagerDepartment.BRANCH_OPERATIONS,
                ManagerDepartment.AUDIT_FRAUD_CONTROL,
                ManagerDepartment.LOAN_OPERATIONS);

        String branchCode = managerProfile.getBranchProfile().getBranchCode();
        return auditLogRepository.findByLoanApplicationIdAndBranchCode(loanApplicationId, branchCode)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AuditLogResponse flagAuditLog(Authentication authentication, Long auditId, AuditFlagRequest request) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        managerAuthorizationService.requireDepartment(managerProfile,
                ManagerDepartment.BRANCH_OPERATIONS,
                ManagerDepartment.AUDIT_FRAUD_CONTROL);

        String branchCode = managerProfile.getBranchProfile().getBranchCode();
        AuditLog auditLog = auditLogRepository.findByIdAndBranchCode(auditId, branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found for branch: " + auditId));

        boolean flagged = request.getFlagged() != null && request.getFlagged();
        auditLog.setIsFlagged(flagged);
        auditLog.setFlagReason(flagged ? request.getReason() : null);

        if (flagged) {
            auditLog.setFlaggedBy(managerProfile.getUsers());
            auditLog.setFlaggedAt(LocalDateTime.now());
        } else {
            auditLog.setFlaggedBy(null);
            auditLog.setFlaggedAt(null);
        }

        AuditLog saved = auditLogRepository.save(auditLog);
        return toResponse(saved);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .auditId(auditLog.getId())
                .auditCode(auditLog.getAuditCode())
                .actionType(auditLog.getActionType())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}


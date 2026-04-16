package com.microfinance.loan.lead.service;

import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.lead.dto.response.AssistedAuditResponse;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.service.ManagerAuthorizationService;
import org.springframework.security.core.Authentication;
import com.microfinance.loan.lead.repository.AssistedActionAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssistedAuditQueryService {

    private final AssistedActionAuditRepository assistedActionAuditRepository;
    private final ManagerAuthorizationService managerAuthorizationService;

    public AssistedAuditQueryService(AssistedActionAuditRepository assistedActionAuditRepository,
                                     ManagerAuthorizationService managerAuthorizationService) {
        this.assistedActionAuditRepository = assistedActionAuditRepository;
        this.managerAuthorizationService = managerAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<AssistedAuditResponse> getLatest(Authentication authentication) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        managerAuthorizationService.requireDepartment(managerProfile,
                ManagerDepartment.BRANCH_OPERATIONS,
                ManagerDepartment.AUDIT_FRAUD_CONTROL,
                ManagerDepartment.LOAN_OPERATIONS);

        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        return assistedActionAuditRepository.findTop100ByLeadBranchProfileBranchCodeOrderByCreatedAtDesc(branchCode)
                .stream()
                .map(audit -> AssistedAuditResponse.builder()
                        .id(audit.getId())
                        .auditCode(audit.getAuditCode())
                        .actionType(audit.getActionType())
                        .remarks(audit.getRemarks())
                        .metadata(audit.getMetadata())
                        .leadId(audit.getLead() != null ? audit.getLead().getId() : null)
                        .userId(audit.getUser() != null ? audit.getUser().getId() : null)
                        .performedByUserId(audit.getPerformedBy() != null ? audit.getPerformedBy().getId() : null)
                        .performedByName(audit.getPerformedBy() != null ? audit.getPerformedBy().getName() : null)
                        .createdAt(audit.getCreatedAt())
                        .build())
                .toList();
    }
}



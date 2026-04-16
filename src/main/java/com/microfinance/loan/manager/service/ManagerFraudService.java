package com.microfinance.loan.manager.service;

import com.microfinance.loan.common.enums.FraudAlertStatus;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.manager.dto.request.FraudAlertActionRequest;
import com.microfinance.loan.manager.dto.response.FraudAlertResponse;
import com.microfinance.loan.manager.entity.FraudAlert;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.FraudAlertRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ManagerFraudService {

    private final FraudAlertRepository fraudAlertRepository;
    private final ManagerAuthorizationService managerAuthorizationService;

    public ManagerFraudService(FraudAlertRepository fraudAlertRepository,
                               ManagerAuthorizationService managerAuthorizationService) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.managerAuthorizationService = managerAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<FraudAlertResponse> getAlerts(Authentication authentication, String status) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        managerAuthorizationService.requireDepartment(managerProfile,
                ManagerDepartment.BRANCH_OPERATIONS,
                ManagerDepartment.AUDIT_FRAUD_CONTROL,
                ManagerDepartment.LOAN_OPERATIONS);

        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        List<FraudAlert> alerts;
        if (StringUtils.hasText(status)) {
            FraudAlertStatus parsed = parseStatus(status);
            alerts = fraudAlertRepository.findByBranchCodeAndAlertStatusOrderByCreatedAtDesc(branchCode, parsed);
        } else {
            alerts = fraudAlertRepository.findByBranchCodeOrderByCreatedAtDesc(branchCode);
        }

        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional
    public FraudAlertResponse applyAction(Authentication authentication, Long alertId, FraudAlertActionRequest request) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        managerAuthorizationService.requireDepartment(managerProfile,
                ManagerDepartment.BRANCH_OPERATIONS,
                ManagerDepartment.AUDIT_FRAUD_CONTROL);
        managerAuthorizationService.requireCanAcknowledgeFraud(managerProfile);

        String branchCode = managerProfile.getBranchProfile().getBranchCode();
        FraudAlert alert = fraudAlertRepository.findByIdAndBranchCode(alertId, branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Fraud alert not found for branch: " + alertId));

        FraudAlertStatus nextStatus = mapActionToStatus(request.getAction());
        alert.setAlertStatus(nextStatus);
        alert.setManagerRemarks(request.getRemarks());

        if (nextStatus == FraudAlertStatus.ACKNOWLEDGED || nextStatus == FraudAlertStatus.INVESTIGATING) {
            alert.setAcknowledgedBy(managerProfile.getUsers());
            alert.setAcknowledgedAt(LocalDateTime.now());
        }

        if (nextStatus == FraudAlertStatus.RESOLVED || nextStatus == FraudAlertStatus.DISMISSED) {
            alert.setResolutionNotes(request.getRemarks());
            alert.setResolvedAt(LocalDateTime.now());
        }

        FraudAlert saved = fraudAlertRepository.save(alert);
        return toResponse(saved);
    }

    private FraudAlertStatus mapActionToStatus(String action) {
        if (!StringUtils.hasText(action)) {
            throw new IllegalArgumentException("Action is required");
        }

        return switch (action.trim().toUpperCase(Locale.ROOT)) {
            case "ACKNOWLEDGE", "ACKNOWLEDGED" -> FraudAlertStatus.ACKNOWLEDGED;
            case "INVESTIGATE", "INVESTIGATING" -> FraudAlertStatus.INVESTIGATING;
            case "RESOLVE", "RESOLVED" -> FraudAlertStatus.RESOLVED;
            case "DISMISS", "DISMISSED" -> FraudAlertStatus.DISMISSED;
            default -> throw new IllegalArgumentException("Unsupported fraud action: " + action);
        };
    }

    private FraudAlertStatus parseStatus(String status) {
        try {
            return FraudAlertStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid fraud status: " + status);
        }
    }

    private FraudAlertResponse toResponse(FraudAlert alert) {
        return FraudAlertResponse.builder()
                .alertId(alert.getId())
                .alertCode(alert.getAlertCode())
                .alertStatus(alert.getAlertStatus() != null ? alert.getAlertStatus().name() : null)
                .riskLevel(alert.getRiskLevel())
                .build();
    }
}


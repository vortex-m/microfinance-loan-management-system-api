package com.microfinance.loan.manager.service;

import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ManagerAuthorizationService {

    private final CurrentUserService currentUserService;
    private final ManagerProfileRepository managerProfileRepository;

    public ManagerAuthorizationService(CurrentUserService currentUserService,
                                       ManagerProfileRepository managerProfileRepository) {
        this.currentUserService = currentUserService;
        this.managerProfileRepository = managerProfileRepository;
    }

    public ManagerProfile getManagerWithBranch(Authentication authentication) {
        Long managerUserId = currentUserService.getCurrentUserId(authentication);
        ManagerProfile profile = managerProfileRepository.findByUsersIdWithBranch(managerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found for user: " + managerUserId));

        if (profile.getBranchProfile() == null) {
            throw new IllegalArgumentException("Manager is not assigned to any branch");
        }

        return profile;
    }

    public void requireDepartment(ManagerProfile profile, ManagerDepartment... departments) {
        String dept = profile.getDepartment();
        if (dept == null) {
            throw new IllegalArgumentException("Manager department is not configured");
        }

        for (ManagerDepartment department : departments) {
            if (department.name().equalsIgnoreCase(dept)) {
                return;
            }
        }

        throw new IllegalArgumentException("Manager department is not allowed for this operation");
    }

    public void requireCanExportReports(ManagerProfile profile) {
        if (!Boolean.TRUE.equals(profile.getCanExportReports())) {
            throw new IllegalArgumentException("Manager is not allowed to export/generate reports");
        }
    }

    public void requireCanAcknowledgeFraud(ManagerProfile profile) {
        if (!Boolean.TRUE.equals(profile.getCanAcknowledgeFraud())) {
            throw new IllegalArgumentException("Manager is not allowed to acknowledge fraud alerts");
        }
    }
}


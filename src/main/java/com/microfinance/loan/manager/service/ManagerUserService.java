package com.microfinance.loan.manager.service;

import com.microfinance.loan.manager.dto.response.ManagerUserResponse;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagerUserService {
    private final UserProfileRepository userProfileRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final CurrentUserService currentUserService;

    public ManagerUserService(UserProfileRepository userProfileRepository,
                              ManagerProfileRepository managerProfileRepository,
                              CurrentUserService currentUserService) {
        this.userProfileRepository = userProfileRepository;
        this.managerProfileRepository = managerProfileRepository;
        this.currentUserService = currentUserService;
    }

    public List<ManagerUserResponse> getAllUser(org.springframework.security.core.Authentication authentication) {
        Long managerUserId = currentUserService.getCurrentUserId(authentication);

        ManagerProfile managerProfile = managerProfileRepository.findByUsersIdWithBranch(managerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found for user: " + managerUserId));

        if (managerProfile.getDepartment() == null
                || !managerProfile.getDepartment().equalsIgnoreCase(ManagerDepartment.BRANCH_OPERATIONS.name())) {
            throw new IllegalArgumentException("Only BRANCH_OPERATIONS manager can access branch user list");
        }

        if (managerProfile.getBranchProfile() == null) {
            throw new IllegalArgumentException("Manager is not mapped to any branch");
        }

        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        return userProfileRepository.findAllWithUsersByBranchCode(branchCode).stream()
                .map(profile -> new ManagerUserResponse(
                        profile.getUsers().getId(),
                        profile.getUsers().getName(),
                        profile.getUsers().getEmail(),
                        profile.getUsers().getRole().name(),
                        branchCode
                ))
                .collect(Collectors.toList());
    }
}

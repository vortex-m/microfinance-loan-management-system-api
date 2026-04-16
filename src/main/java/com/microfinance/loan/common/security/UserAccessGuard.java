package com.microfinance.loan.common.security;

import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import com.microfinance.loan.common.service.impl.UserDetailsServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAccessGuard")
public class UserAccessGuard {
    private final UserDetailsServiceImpl userDetailsService;
    private final ManagerProfileRepository managerProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final UserProfileRepository userProfileRepository;

    public UserAccessGuard(UserDetailsServiceImpl userDetailsService,
                           ManagerProfileRepository managerProfileRepository,
                           OfficerProfileRepository officerProfileRepository,
                           UserProfileRepository userProfileRepository){
        this.userDetailsService = userDetailsService;
        this.managerProfileRepository = managerProfileRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public boolean canAccessUser(Long userId, Authentication auth){
        if(auth == null || userId == null){
            return false;
        }
        return userDetailsService.resolveUser(auth.getName())
                .map(u -> userId.equals((u.getId())))
                .orElse(false);
    }

    public boolean hasRole(Authentication auth, Role role) {
        if (auth == null || role == null) {
            return false;
        }
        return userDetailsService.resolveUser(auth.getName())
                .map(u -> u.getRole() == role)
                .orElse(false);
    }

    public boolean canAccessBranch(String branchCode, Authentication auth) {
        if (auth == null || branchCode == null || branchCode.trim().isEmpty()) {
            return false;
        }

        String normalizedBranchCode = branchCode.trim().toUpperCase();
        return userDetailsService.resolveUser(auth.getName()).map(user -> {
            if (user.getRole() == Role.ADMIN) {
                return true;
            }
            if (user.getRole() == Role.MANAGER) {
                return managerProfileRepository.findByUsersIdWithBranch(user.getId())
                        .map(mp -> mp.getBranchProfile() != null
                                && normalizedBranchCode.equalsIgnoreCase(mp.getBranchProfile().getBranchCode()))
                        .orElse(false);
            }
            if (user.getRole() == Role.OFFICER) {
                return officerProfileRepository.findByUsersIdWithBranch(user.getId())
                        .map(op -> op.getBranchProfile() != null
                                && normalizedBranchCode.equalsIgnoreCase(op.getBranchProfile().getBranchCode()))
                        .orElse(false);
            }
            if (user.getRole() == Role.USER) {
                return userProfileRepository.findByUsersId(user.getId())
                        .map(up -> up.getBranchProfile() != null
                                && normalizedBranchCode.equalsIgnoreCase(up.getBranchProfile().getBranchCode()))
                        .orElse(false);
            }
            return false;
        }).orElse(false);
    }
}

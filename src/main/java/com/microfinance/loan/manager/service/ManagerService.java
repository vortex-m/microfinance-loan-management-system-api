package com.microfinance.loan.manager.service;

import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.enums.CashSettlementStatus;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.common.enums.UserStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.request.ManagerProfileUpdateRequest;
import com.microfinance.loan.manager.dto.response.DashboardResponse;
import com.microfinance.loan.manager.dto.response.ManagerProfileResponse;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.payment.repository.TransactionRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class ManagerService {

    private final StaffService staffService;
    private final CurrentUserService currentUserService;
    private final ManagerProfileRepository managerProfileRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserProfileRepository userProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final TransactionRepository transactionRepository;

    public ManagerService(StaffService staffService,
                          CurrentUserService currentUserService,
                          ManagerProfileRepository managerProfileRepository,
                          LoanApplicationRepository loanApplicationRepository,
                          UserProfileRepository userProfileRepository,
                          OfficerProfileRepository officerProfileRepository,
                          AgentProfileRepository agentProfileRepository,
                          TransactionRepository transactionRepository) {
        this.staffService = staffService;
        this.currentUserService = currentUserService;
        this.managerProfileRepository = managerProfileRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userProfileRepository = userProfileRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.transactionRepository = transactionRepository;
    }

    public StaffCreateResponse createAgent(Authentication authentication, CreateStaffRequest request) {
        ManagerProfile managerProfile = getBranchOperationsManager(authentication);
        return staffService.createAgent(managerProfile, request);
    }

    public StaffCreateResponse createManager(Authentication authentication, CreateStaffRequest request) {
        ManagerProfile managerProfile = getBranchOperationsManager(authentication);
        return staffService.createManager(managerProfile, request);
    }

    public StaffCreateResponse createOfficer(Authentication authentication, CreateStaffRequest request) {
        ManagerProfile managerProfile = getBranchOperationsManager(authentication);
        return staffService.createOfficer(managerProfile, request);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Authentication authentication) {
        ManagerProfile managerProfile = getManagerForDashboard(authentication);
        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        long pendingOfficer = loanApplicationRepository.countByBranchCodeAndStatuses(
                branchCode,
                List.of(LoanStatus.PENDING, LoanStatus.UNDER_REVIEW)
        );
        long pendingManager = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.PENDING_MANAGER_APPROVAL);
        long approved = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.APPROVED);
        long disbursed = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.DISBURSED);
        long rejected = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.REJECTED);
        long closed = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.CLOSED);
        Double totalPortfolio = loanApplicationRepository.sumTotalAmountByBranchCode(branchCode);

        long activeUsers = userProfileRepository.countByBranchCodeAndUserStatus(branchCode, UserStatus.ACTIVE);
        long activeOfficers = officerProfileRepository.countByBranchProfileBranchCodeAndOfficerStatus(branchCode, OfficerStatus.ACTIVE);
        long activeAgents = agentProfileRepository.countByBranchProfileBranchCodeAndAgentStatus(branchCode, AgentStatus.ACTIVE);
        double unsettled = safeAmount(transactionRepository.sumCashAmountByBranchAndSettlementStatus(
                branchCode, CashSettlementStatus.COLLECTED_UNSETTLED));
        double settled = safeAmount(transactionRepository.sumCashAmountByBranchAndSettlementStatus(
                branchCode, CashSettlementStatus.SETTLED));

        return DashboardResponse.builder()
                .totalPendingLoans(Math.toIntExact(pendingOfficer + pendingManager))
                .totalFraudAlerts(0)
                .totalOpenAudits(0)
                .totalActiveStaff(Math.toIntExact(activeOfficers + activeAgents))
                .pendingOfficerReview(Math.toIntExact(pendingOfficer))
                .pendingManagerApproval(Math.toIntExact(pendingManager))
                .approvedLoans(Math.toIntExact(approved))
                .disbursedLoans(Math.toIntExact(disbursed))
                .rejectedLoans(Math.toIntExact(rejected))
                .closedLoans(Math.toIntExact(closed))
                .activeUsers(Math.toIntExact(activeUsers))
                .activeOfficers(Math.toIntExact(activeOfficers))
                .activeAgents(Math.toIntExact(activeAgents))
                .totalPortfolioAmount(totalPortfolio == null ? 0d : Math.round(totalPortfolio * 100d) / 100d)
                .totalCollectedCash(round(unsettled + settled))
                .totalSettledCash(round(settled))
                .totalUnsettledCash(round(unsettled))
                .build();
    }

    @Transactional
    public ManagerProfileResponse updateProfile(org.springframework.security.core.Authentication authentication,
                                                ManagerProfileUpdateRequest request) {
        Long userId = currentUserService.getCurrentUserId(authentication);
        ManagerProfile profile = managerProfileRepository.findByUsersIdWithBranch(userId)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found for user: " + userId));

        if (StringUtils.hasText(request.getDesignation())) {
            profile.setDesignation(request.getDesignation().trim());
        }
        if (StringUtils.hasText(request.getAddress())) {
            profile.setAddress(request.getAddress().trim());
        }
        if (StringUtils.hasText(request.getCity())) {
            profile.setCity(request.getCity().trim());
        }
        if (StringUtils.hasText(request.getState())) {
            profile.setState(request.getState().trim());
        }
        if (StringUtils.hasText(request.getPinCode())) {
            profile.setPinCode(request.getPinCode().trim());
        }
        if (StringUtils.hasText(request.getFathersName())) {
            profile.setFathersName(request.getFathersName().trim());
        }
        if (StringUtils.hasText(request.getMothersName())) {
            profile.setMothersName(request.getMothersName().trim());
        }
        if (StringUtils.hasText(request.getAadhaarNumber())) {
            profile.setAadhaarNumber(request.getAadhaarNumber().trim());
        }
        if (StringUtils.hasText(request.getPanNumber())) {
            profile.setPanNumber(request.getPanNumber().trim().toUpperCase());
        }

        ManagerProfile saved = managerProfileRepository.save(profile);
        return toResponse(saved);
    }

    private ManagerProfileResponse toResponse(ManagerProfile profile) {
        String branch = profile.getBranchProfile() != null ? profile.getBranchProfile().getBranchName() : profile.getBranch();
        String branchCode = profile.getBranchProfile() != null ? profile.getBranchProfile().getBranchCode() : profile.getBranchCode();
        String region = profile.getBranchProfile() != null ? profile.getBranchProfile().getRegionName() : profile.getRegion();
        String regionCode = profile.getBranchProfile() != null ? profile.getBranchProfile().getRegionCode() : profile.getRegionCode();

        return ManagerProfileResponse.builder()
                .userId(profile.getUsers().getId())
                .managerCode(profile.getManagerCode())
                .designation(profile.getDesignation())
                .department(profile.getDepartment())
                .branch(branch)
                .branchCode(branchCode)
                .region(region)
                .regionCode(regionCode)
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .pinCode(profile.getPinCode())
                .fathersName(profile.getFathersName())
                .mothersName(profile.getMothersName())
                .aadhaarNumber(profile.getAadhaarNumber())
                .panNumber(profile.getPanNumber())
                .build();
    }

    private ManagerProfile getBranchOperationsManager(Authentication authentication) {
        Long userId = currentUserService.getCurrentUserId(authentication);
        ManagerProfile managerProfile = managerProfileRepository.findByUsersIdWithBranch(userId)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found for user: " + userId));

        if (managerProfile.getDepartment() == null
                || !managerProfile.getDepartment().equalsIgnoreCase(ManagerDepartment.BRANCH_OPERATIONS.name())) {
            throw new IllegalArgumentException("Only BRANCH_OPERATIONS manager can create branch staff");
        }

        BranchProfile branchProfile = managerProfile.getBranchProfile();
        if (branchProfile == null) {
            throw new IllegalArgumentException("Manager is not assigned to any branch");
        }
        if (Boolean.FALSE.equals(branchProfile.getActive())) {
            throw new IllegalArgumentException("Assigned branch is inactive");
        }

        return managerProfile;
    }

    private ManagerProfile getManagerForDashboard(Authentication authentication) {
        Long userId = currentUserService.getCurrentUserId(authentication);
        ManagerProfile managerProfile = managerProfileRepository.findByUsersIdWithBranch(userId)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found for user: " + userId));
        if (managerProfile.getBranchProfile() == null) {
            throw new IllegalArgumentException("Manager is not assigned to any branch");
        }
        return managerProfile;
    }

    private double safeAmount(Double amount) {
        return amount == null ? 0d : amount;
    }

    private double round(double value) {
        return Math.round(value * 100d) / 100d;
    }
}

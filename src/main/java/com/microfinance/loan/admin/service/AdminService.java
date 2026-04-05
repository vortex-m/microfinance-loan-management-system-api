package com.microfinance.loan.admin.service;

import com.microfinance.loan.admin.dto.request.AdminProfileUpsertRequest;
import com.microfinance.loan.admin.dto.request.CreateAdminRequest;
import com.microfinance.loan.admin.dto.response.AdminProfileResponse;
import com.microfinance.loan.admin.dto.request.CreateBranchOperationManagerRequest;
import com.microfinance.loan.admin.dto.response.BranchMetricsResponse;
import com.microfinance.loan.admin.dto.response.CreateAdminResponse;
import com.microfinance.loan.admin.dto.response.CreateBranchOperationManagerResponse;
import com.microfinance.loan.admin.entity.AdminProfile;
import com.microfinance.loan.admin.repository.AdminProfileRepository;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.branch.repository.BranchProfileRepository;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.enums.UserStatus;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.manager.support.GeneratePass;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final CurrentUserService currentUserService;
    private final ManagerProfileRepository managerProfileRepository;
    private final BranchProfileRepository branchProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final GeneratePass generatePass;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminService(UserRepository userRepository,
                        AdminProfileRepository adminProfileRepository,
                        CurrentUserService currentUserService,
                        ManagerProfileRepository managerProfileRepository,
                        BranchProfileRepository branchProfileRepository,
                        UserProfileRepository userProfileRepository,
                        OfficerProfileRepository officerProfileRepository,
                        AgentProfileRepository agentProfileRepository,
                        LoanApplicationRepository loanApplicationRepository,
                        GeneratePass generatePass,
                        PasswordEncoder passwordEncoder,
                        MailService mailService) {
        this.userRepository = userRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.currentUserService = currentUserService;
        this.managerProfileRepository = managerProfileRepository;
        this.branchProfileRepository = branchProfileRepository;
        this.userProfileRepository = userProfileRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.generatePass = generatePass;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public CreateAdminResponse bootstrapAdmin(CreateAdminRequest req){
        long adminCount = userRepository.countByRole(Role.ADMIN);
        if(adminCount > 0){
            throw new IllegalArgumentException("Bootstrap is disabled. Admin already exists!");
        }
        return createAdminInternal(req, "Bootstrap admin created successfully.");
    }

    @Transactional
    public CreateAdminResponse createAdmin(CreateAdminRequest req){
        return createAdminInternal(req, "Admin created successfully.");
    }


    @Transactional(readOnly = true)
    public AdminProfileResponse getMyProfile(org.springframework.security.core.Authentication authentication) {
        Users adminUser = getAdminUser(authentication);

        AdminProfile profile = adminProfileRepository.findByUsersId(adminUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found. Please complete profile first."));

        return toAdminProfileResponse(profile, adminUser);
    }

    @Transactional
    public AdminProfileResponse upsertMyProfile(org.springframework.security.core.Authentication authentication,
                                                AdminProfileUpsertRequest request) {
        Users adminUser = getAdminUser(authentication);

        AdminProfile profile = adminProfileRepository.findByUsersId(adminUser.getId())
                .orElseGet(() -> AdminProfile.builder().users(adminUser).build());

        profile.setCompanyName(request.getCompanyName().trim());
        profile.setCompanyCode(trimOrNull(request.getCompanyCode()));
        profile.setLegalEntityName(trimOrNull(request.getLegalEntityName()));
        profile.setGstNumber(trimOrNull(request.getGstNumber()));
        profile.setPanNumber(trimOrNull(request.getPanNumber()));
        profile.setAdminFullName(request.getAdminFullName().trim());
        profile.setDesignation(trimOrNull(request.getDesignation()));
        profile.setOfficePhone(trimOrNull(request.getOfficePhone()));
        profile.setWebsite(trimOrNull(request.getWebsite()));
        profile.setAddress(trimOrNull(request.getAddress()));
        profile.setCity(trimOrNull(request.getCity()));
        profile.setState(trimOrNull(request.getState()));
        profile.setCountry(trimOrNull(request.getCountry()));
        profile.setPinCode(trimOrNull(request.getPinCode()));

        AdminProfile saved = adminProfileRepository.save(profile);
        return toAdminProfileResponse(saved, adminUser);
    }

    @Transactional
    public CreateBranchOperationManagerResponse createBranchOperationManager(CreateBranchOperationManagerRequest request) {
        String managerCode = request.getManagerCode().trim().toUpperCase();
        String branchCode = request.getBranchCode().trim().toUpperCase();

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone().trim())) {
            throw new IllegalArgumentException("Phone already exists: " + request.getPhone());
        }
        if (managerProfileRepository.existsByManagerCode(managerCode)) {
            throw new IllegalArgumentException("Manager code already exists: " + managerCode);
        }
        if (managerProfileRepository.existsByBranchProfileBranchCodeAndDepartmentIgnoreCase(
                branchCode,
                ManagerDepartment.BRANCH_OPERATIONS.name())) {
            throw new IllegalArgumentException("Branch already has a BRANCH_OPERATIONS manager: " + branchCode);
        }

        BranchProfile branchProfile = branchProfileRepository.findByBranchCodeIgnoreCase(branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchCode));

        if (Boolean.FALSE.equals(branchProfile.getActive())) {
            throw new IllegalArgumentException("Branch is inactive: " + branchCode);
        }

        String tempPassword = generatePass.generatePassFromCode(managerCode);

        Users managerUser = Users.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .password(passwordEncoder.encode(tempPassword))
                .role(Role.MANAGER)
                .isHome(false)
                .address(request.getAddress())
                .build();

        Users savedUser = userRepository.save(managerUser);

        ManagerProfile managerProfile = ManagerProfile.builder()
                .users(savedUser)
                .managerCode(managerCode)
                .designation("Branch Operations Manager")
                .department(ManagerDepartment.BRANCH_OPERATIONS.name())
                .branch(branchProfile.getBranchName())
                .branchCode(branchProfile.getBranchCode())
                .region(branchProfile.getRegionName())
                .regionCode(branchProfile.getRegionCode())
                .branchProfile(branchProfile)
                .accessLevel("STANDARD")
                .build();

        managerProfileRepository.save(managerProfile);

        String message = "BRANCH_OPERATIONS manager created and credentials sent on email";
        try {
            mailService.sendStaffCredentials(savedUser.getEmail(), savedUser.getName(), "MANAGER", managerCode, tempPassword);
        } catch (IllegalStateException ex) {
            message = "BRANCH_OPERATIONS manager created, but email delivery failed. Share temp password manually.";
        }

        return CreateBranchOperationManagerResponse.builder()
                .userId(savedUser.getId())
                .managerCode(managerCode)
                .department(ManagerDepartment.BRANCH_OPERATIONS.name())
                .branchCode(branchProfile.getBranchCode())
                .email(savedUser.getEmail())
                .tempPassword(tempPassword)
                .message(message)
                .build();
    }

    @Transactional(readOnly = true)
    public BranchMetricsResponse getBranchMetrics(String branchCode) {
        String normalizedBranchCode = branchCode.trim().toUpperCase();

        BranchProfile branchProfile = branchProfileRepository.findByBranchCodeIgnoreCase(normalizedBranchCode)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + normalizedBranchCode));

        long totalUsers = userProfileRepository.countByBranchCode(normalizedBranchCode);
        long activeUsers = userProfileRepository.countByBranchCodeAndUserStatus(normalizedBranchCode, UserStatus.ACTIVE);
        long inactiveUsers = totalUsers - activeUsers;

        long totalOfficers = officerProfileRepository.countByBranchProfileBranchCode(normalizedBranchCode);
        long activeOfficers = officerProfileRepository.countByBranchProfileBranchCodeAndOfficerStatus(normalizedBranchCode, OfficerStatus.ACTIVE);
        long totalAgents = agentProfileRepository.countByBranchProfileBranchCode(normalizedBranchCode);
        long activeAgents = agentProfileRepository.countByBranchProfileBranchCodeAndAgentStatus(normalizedBranchCode, AgentStatus.ACTIVE);

        long totalLoans = loanApplicationRepository.countByBranchCode(normalizedBranchCode);
        long pendingLoans = loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.PENDING)
                + loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.UNDER_REVIEW)
                + loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.PENDING_MANAGER_APPROVAL);
        long approvedLoans = loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.APPROVED);
        long rejectedLoans = loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.REJECTED);
        long disbursedLoans = loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.DISBURSED);
        long closedLoans = loanApplicationRepository.countByBranchCodeAndStatus(normalizedBranchCode, LoanStatus.CLOSED);
        long activeLoans = approvedLoans + disbursedLoans;
        Double totalLoanAmount = loanApplicationRepository.sumTotalAmountByBranchCode(normalizedBranchCode);

        return BranchMetricsResponse.builder()
                .branchCode(branchProfile.getBranchCode())
                .branchName(branchProfile.getBranchName())
                .regionCode(branchProfile.getRegionCode())
                .regionName(branchProfile.getRegionName())
                .active(branchProfile.getActive())
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalOfficers(totalOfficers)
                .activeOfficers(activeOfficers)
                .totalAgents(totalAgents)
                .activeAgents(activeAgents)
                .totalLoans(totalLoans)
                .pendingLoans(pendingLoans)
                .approvedLoans(approvedLoans)
                .rejectedLoans(rejectedLoans)
                .disbursedLoans(disbursedLoans)
                .closedLoans(closedLoans)
                .activeLoans(activeLoans)
                .totalLoanAmount(totalLoanAmount == null ? 0D : totalLoanAmount)
                .build();
    }

    private Users getAdminUser(org.springframework.security.core.Authentication authentication) {
        Long userId = currentUserService.getCurrentUserId(authentication);
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admin can access this operation");
        }

        return user;
    }

    private AdminProfileResponse toAdminProfileResponse(AdminProfile profile, Users user) {
        return AdminProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .companyName(profile.getCompanyName())
                .companyCode(profile.getCompanyCode())
                .legalEntityName(profile.getLegalEntityName())
                .gstNumber(profile.getGstNumber())
                .panNumber(profile.getPanNumber())
                .adminFullName(profile.getAdminFullName())
                .designation(profile.getDesignation())
                .officePhone(profile.getOfficePhone())
                .website(profile.getWebsite())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .country(profile.getCountry())
                .pinCode(profile.getPinCode())
                .build();
    }

    private CreateAdminResponse createAdminInternal(CreateAdminRequest req, String message){
        String email = req.getEmail().trim().toLowerCase();
        String phone = req.getPhone().trim();

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        if(userRepository.existsByPhone(phone)){
            throw new IllegalArgumentException("Phone number already exists: " + phone);
        }

        Users adminUser = Users.builder()
                .name(req.getName().trim())
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN)
                .isHome(true)
                .address(trimOrNull(req.getAddress()))
                .build();
        Users savedUser = userRepository.save(adminUser);

        AdminProfile profile = AdminProfile.builder()
                .users(savedUser)
                .companyName(req.getCompanyName().trim())
                .companyCode(trimOrNull(req.getCompanyCode()))
                .gstNumber(trimOrNull(req.getGstNumber()))
                .legalEntityName(trimOrNull(req.getLegalEntityName()))
                .panNumber(trimOrNull(req.getPanNumber()))
                .adminFullName(req.getName().trim())
                .designation(trimOrNull(req.getDesignation()))
                .officePhone(trimOrNull(req.getCompanyPhone()))
                .website(trimOrNull(req.getCompanyWebsite()))
                .address(trimOrNull(StringUtils.hasText(req.getCompanyAddress()) ? req.getCompanyAddress() : req.getAddress()))
                .state(trimOrNull(req.getState()))
                .city(trimOrNull(req.getCity()))
                .country(trimOrNull(req.getCountry()))
                .pinCode(trimOrNull(req.getPinCode()))
                .build();
        adminProfileRepository.save(profile);

        return CreateAdminResponse.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .message(message)
                .build();
    }

    private String trimOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}




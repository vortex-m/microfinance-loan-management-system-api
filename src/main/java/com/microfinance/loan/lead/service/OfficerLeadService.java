package com.microfinance.loan.lead.service;

import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.*;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.lead.dto.request.ConvertLeadToUserRequest;
import com.microfinance.loan.lead.dto.request.OfficerLeadDecisionRequest;
import com.microfinance.loan.lead.dto.response.LeadResponse;
import com.microfinance.loan.lead.entity.AgentLead;
import com.microfinance.loan.lead.repository.AgentLeadRepository;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfficerLeadService {

    private final AgentLeadRepository agentLeadRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AssistedAuditService assistedAuditService;

    public OfficerLeadService(AgentLeadRepository agentLeadRepository,
                              OfficerProfileRepository officerProfileRepository,
                              UserRepository userRepository,
                              UserProfileRepository userProfileRepository,
                              PasswordEncoder passwordEncoder,
                              AssistedAuditService assistedAuditService) {
        this.agentLeadRepository = agentLeadRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.assistedAuditService = assistedAuditService;
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getQueue(Long officerUserId) {
        officerProfileRepository.findByUsersIdWithBranch(officerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Officer profile not found: " + officerUserId));

        return agentLeadRepository.findByAssignedOfficerIdAndStatusInOrderByCreatedAtAsc(
                        officerUserId,
                        List.of(LeadStatus.SUBMITTED_TO_OFFICER, LeadStatus.REVERIFY_REQUIRED)
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LeadResponse decide(Long officerUserId, Long leadId, OfficerLeadDecisionRequest request) {
        AgentLead lead = agentLeadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        OfficerProfile officerProfile = officerProfileRepository.findByUsersIdWithBranch(officerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Officer profile not found: " + officerUserId));

        if (lead.getAssignedOfficer() == null || !lead.getAssignedOfficer().getId().equals(officerUserId)) {
            throw new IllegalArgumentException("Lead is not assigned to this officer");
        }

        BranchProfile officerBranch = officerProfile.getBranchProfile();
        if (officerBranch == null || lead.getBranchProfile() == null
                || !officerBranch.getBranchCode().equalsIgnoreCase(lead.getBranchProfile().getBranchCode())) {
            throw new IllegalArgumentException("Officer can access only own branch leads");
        }

        switch (request.getDecision()) {
            case APPROVE_FOR_MANAGER -> lead.setStatus(LeadStatus.OFFICER_APPROVED);
            case REVERIFY -> lead.setStatus(LeadStatus.REVERIFY_REQUIRED);
            case REJECT -> lead.setStatus(LeadStatus.REJECTED);
        }
        lead.setOfficerRemarks(request.getRemarks());

        AgentLead saved = agentLeadRepository.save(lead);
        Users officerUser = officerProfile.getUsers();
        assistedAuditService.log(saved, null, officerUser, AssistedActionType.OFFICER_DECISION,
                "Officer lead decision captured", request.getDecision().name());
        return toResponse(saved);
    }

    @Transactional
    public LeadResponse convertToUser(Long officerUserId, Long leadId, ConvertLeadToUserRequest request) {
        AgentLead lead = agentLeadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        if (lead.getAssignedOfficer() == null || !lead.getAssignedOfficer().getId().equals(officerUserId)) {
            throw new IllegalArgumentException("Lead is not assigned to this officer");
        }

        if (lead.getStatus() != LeadStatus.OFFICER_APPROVED) {
            throw new IllegalArgumentException("Lead must be OFFICER_APPROVED before conversion");
        }

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already exists: " + phone);
        }

        Users user = Users.builder()
                .name(lead.getFullName().trim())
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(request.getTemporaryPassword()))
                .role(Role.USER)
                .isHome(false)
                .address(lead.getAddress())
                .build();
        Users savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .users(savedUser)
                .branchProfile(lead.getBranchProfile())
                .fatherName(lead.getFatherName())
                .motherName(lead.getMotherName())
                .dateOfBirth(lead.getDateOfBirth())
                .occupation(lead.getOccupation())
                .maritalStatus(lead.getMaritalStatus())
                .monthlyIncome(lead.getMonthlyIncome())
                .street(lead.getAddress())
                .city(lead.getCity())
                .state(lead.getState())
                .pinCode(lead.getPinCode())
                .aadhaarNumber(lead.getAadhaarNumber())
                .panNumber(lead.getPanNumber())
                .kycStatus(KycStatus.PENDING)
                .originChannel(OriginChannel.AGENT_ASSISTED)
                .assistedByAgent(lead.getAgent())
                .consentMode(lead.getConsentMode())
                .build();
        userProfileRepository.save(profile);

        lead.setConvertedUser(savedUser);
        lead.setStatus(LeadStatus.CONVERTED_TO_USER);
        AgentLead savedLead = agentLeadRepository.save(lead);

        assistedAuditService.log(savedLead, savedUser, lead.getAssignedOfficer(), AssistedActionType.LEAD_CONVERTED_TO_USER,
                "Lead converted to user", "userId=" + savedUser.getId());
        return toResponse(savedLead);
    }

    private LeadResponse toResponse(AgentLead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .leadCode(lead.getLeadCode())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .guardianPhone(lead.getGuardianPhone())
                .email(lead.getEmail())
                .village(lead.getVillage())
                .address(lead.getAddress())
                .fatherName(lead.getFatherName())
                .motherName(lead.getMotherName())
                .dateOfBirth(lead.getDateOfBirth())
                .maritalStatus(lead.getMaritalStatus())
                .occupation(lead.getOccupation())
                .monthlyIncome(lead.getMonthlyIncome())
                .city(lead.getCity())
                .state(lead.getState())
                .pinCode(lead.getPinCode())
                .aadhaarNumber(lead.getAadhaarNumber())
                .panNumber(lead.getPanNumber())
                .aadhaarFileUrl(lead.getAadhaarFileUrl())
                .panFileUrl(lead.getPanFileUrl())
                .branchCode(lead.getBranchProfile() != null ? lead.getBranchProfile().getBranchCode() : null)
                .branchName(lead.getBranchProfile() != null ? lead.getBranchProfile().getBranchName() : null)
                .agentUserId(lead.getAgent() != null ? lead.getAgent().getId() : null)
                .assignedOfficerUserId(lead.getAssignedOfficer() != null ? lead.getAssignedOfficer().getId() : null)
                .convertedUserId(lead.getConvertedUser() != null ? lead.getConvertedUser().getId() : null)
                .status(lead.getStatus())
                .consentMode(lead.getConsentMode())
                .consentText(lead.getConsentText())
                .consentProofUrl(lead.getConsentProofUrl())
                .witnessName(lead.getWitnessName())
                .witnessPhone(lead.getWitnessPhone())
                .requestedAmount(lead.getRequestedAmount())
                .tenureMonths(lead.getTenureMonths())
                .loanPurpose(lead.getLoanPurpose())
                .disbursalMode(lead.getDisbursalMode())
                .disbursalBankName(lead.getDisbursalBankName())
                .disbursalBankAccount(lead.getDisbursalBankAccount())
                .disbursalIfscCode(lead.getDisbursalIfscCode())
                .officerRemarks(lead.getOfficerRemarks())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }
}


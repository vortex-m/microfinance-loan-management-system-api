package com.microfinance.loan.lead.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.branch.repository.BranchProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.*;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.lead.dto.request.*;
import com.microfinance.loan.lead.dto.response.LeadResponse;
import com.microfinance.loan.lead.entity.AgentLead;
import com.microfinance.loan.lead.repository.AgentLeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AgentLeadService {

    private final AgentLeadRepository agentLeadRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final BranchProfileRepository branchProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AssistedAuditService assistedAuditService;

    public AgentLeadService(AgentLeadRepository agentLeadRepository,
                            AgentProfileRepository agentProfileRepository,
                            BranchProfileRepository branchProfileRepository,
                            UserRepository userRepository,
                            FileStorageService fileStorageService,
                            AssistedAuditService assistedAuditService) {
        this.agentLeadRepository = agentLeadRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.branchProfileRepository = branchProfileRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.assistedAuditService = assistedAuditService;
    }

    @Transactional
    public LeadResponse createLead(Long agentUserId, CreateLeadRequest request) {
        Users agentUser = userRepository.findById(agentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Agent user not found: " + agentUserId));
        AgentProfile agentProfile = agentProfileRepository.findByUsersIdWithBranch(agentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Agent profile not found: " + agentUserId));

        BranchProfile branchProfile = branchProfileRepository.findByBranchCodeIgnoreCase(request.getBranchCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + request.getBranchCode()));

        if (agentProfile.getBranchProfile() == null
                || !branchProfile.getBranchCode().equalsIgnoreCase(agentProfile.getBranchProfile().getBranchCode())) {
            throw new IllegalArgumentException("Agent can create lead only for own branch");
        }

        AgentLead lead = AgentLead.builder()
                .leadCode(generateLeadCode())
                .fullName(request.getFullName().trim())
                .phone(safeTrim(request.getPhone()))
                .guardianPhone(safeTrim(request.getGuardianPhone()))
                .email(safeTrim(request.getEmail()))
                .village(safeTrim(request.getVillage()))
                .address(request.getAddress().trim())
                .branchProfile(branchProfile)
                .agent(agentUser)
                .status(LeadStatus.NEW)
                .originChannel(OriginChannel.AGENT_ASSISTED)
                .consentMode(request.getConsentMode())
                .consentText(safeTrim(request.getConsentText()))
                .build();

        AgentLead saved = agentLeadRepository.save(lead);
        assistedAuditService.log(saved, null, agentUser, AssistedActionType.LEAD_CREATED,
                "Lead created by agent", null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getMyLeads(Long agentUserId) {
        return agentLeadRepository.findByAgentIdOrderByCreatedAtDesc(agentUserId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse getMyLead(Long agentUserId, Long leadId) {
        AgentLead lead = agentLeadRepository.findByIdAndAgentId(leadId, agentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found for this agent: " + leadId));
        return toResponse(lead);
    }

    @Transactional
    public LeadResponse updateProfile(Long agentUserId, Long leadId, LeadProfileUpdateRequest request) {
        AgentLead lead = requireOwnedLead(agentUserId, leadId);

        if (StringUtils.hasText(request.getFatherName())) {
            lead.setFatherName(request.getFatherName().trim());
        }
        if (StringUtils.hasText(request.getMotherName())) {
            lead.setMotherName(request.getMotherName().trim());
        }
        if (request.getDateOfBirth() != null) {
            lead.setDateOfBirth(request.getDateOfBirth());
        }
        if (StringUtils.hasText(request.getMaritalStatus())) {
            lead.setMaritalStatus(request.getMaritalStatus().trim());
        }
        if (StringUtils.hasText(request.getOccupation())) {
            lead.setOccupation(request.getOccupation().trim());
        }
        if (request.getMonthlyIncome() != null) {
            lead.setMonthlyIncome(request.getMonthlyIncome());
        }
        if (StringUtils.hasText(request.getAddress())) {
            lead.setAddress(request.getAddress().trim());
        }
        if (StringUtils.hasText(request.getVillage())) {
            lead.setVillage(request.getVillage().trim());
        }
        if (StringUtils.hasText(request.getCity())) {
            lead.setCity(request.getCity().trim());
        }
        if (StringUtils.hasText(request.getState())) {
            lead.setState(request.getState().trim());
        }
        if (StringUtils.hasText(request.getPinCode())) {
            lead.setPinCode(request.getPinCode().trim());
        }
        if (StringUtils.hasText(request.getAadhaarNumber())) {
            lead.setAadhaarNumber(request.getAadhaarNumber().trim());
        }
        if (StringUtils.hasText(request.getPanNumber())) {
            lead.setPanNumber(request.getPanNumber().trim().toUpperCase(Locale.ROOT));
        }

        if (lead.getStatus() == LeadStatus.NEW) {
            lead.setStatus(LeadStatus.PROFILE_CAPTURED);
        }

        AgentLead saved = agentLeadRepository.save(lead);
        assistedAuditService.log(saved, null, lead.getAgent(), AssistedActionType.PROFILE_UPDATED,
                "Lead profile updated", null);
        return toResponse(saved);
    }

    @Transactional
    public LeadResponse uploadKyc(Long agentUserId, Long leadId, KycDocumentType documentType, MultipartFile file) throws IOException {
        AgentLead lead = requireOwnedLead(agentUserId, leadId);

        String fileUrl = fileStorageService.storeFile(file, "lead-kyc/" + leadId + "/" + documentType.name().toLowerCase(Locale.ROOT));
        if (documentType == KycDocumentType.AADHAAR) {
            lead.setAadhaarFileUrl(fileUrl);
        } else if (documentType == KycDocumentType.PAN) {
            lead.setPanFileUrl(fileUrl);
        }

        if (lead.getAadhaarFileUrl() != null || lead.getPanFileUrl() != null) {
            lead.setStatus(LeadStatus.KYC_UPLOADED);
        }

        AgentLead saved = agentLeadRepository.save(lead);
        assistedAuditService.log(saved, null, lead.getAgent(), AssistedActionType.KYC_UPLOADED,
                "Lead KYC document uploaded", documentType.name());
        return toResponse(saved);
    }

    @Transactional
    public LeadResponse captureConsent(Long agentUserId, Long leadId, LeadConsentRequest request) {
        AgentLead lead = requireOwnedLead(agentUserId, leadId);

        lead.setConsentMode(request.getConsentMode());
        lead.setConsentText(request.getConsentText().trim());
        lead.setConsentProofUrl(safeTrim(request.getConsentProofUrl()));
        lead.setWitnessName(safeTrim(request.getWitnessName()));
        lead.setWitnessPhone(safeTrim(request.getWitnessPhone()));

        AgentLead saved = agentLeadRepository.save(lead);
        assistedAuditService.log(saved, null, lead.getAgent(), AssistedActionType.CONSENT_CAPTURED,
                "Lead consent captured", request.getConsentMode().name());
        return toResponse(saved);
    }

    @Transactional
    public LeadResponse createLoanDraft(Long agentUserId, Long leadId, LeadLoanDraftRequest request) {
        AgentLead lead = requireOwnedLead(agentUserId, leadId);

        lead.setRequestedAmount(request.getRequestedAmount());
        lead.setTenureMonths(request.getTenureMonths());
        lead.setLoanPurpose(request.getLoanPurpose().trim());
        lead.setDisbursalMode(request.getDisbursalMode());
        lead.setDisbursalBankName(safeTrim(request.getDisbursalBankName()));
        lead.setDisbursalBankAccount(safeTrim(request.getDisbursalBankAccount()));
        lead.setDisbursalIfscCode(safeTrim(request.getDisbursalIfscCode()));

        AgentLead saved = agentLeadRepository.save(lead);
        assistedAuditService.log(saved, null, lead.getAgent(), AssistedActionType.LOAN_DRAFT_CREATED,
                "Loan draft captured for lead", null);
        return toResponse(saved);
    }

    @Transactional
    public LeadResponse submitToOfficer(Long agentUserId, Long leadId, SubmitLeadToOfficerRequest request) {
        AgentLead lead = requireOwnedLead(agentUserId, leadId);

        if (request.getOfficerUserId() == null) {
            throw new IllegalArgumentException("Officer userId is required");
        }

        Users officerUser = userRepository.findById(request.getOfficerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Officer user not found: " + request.getOfficerUserId()));

        if (officerUser.getRole() != Role.OFFICER) {
            throw new IllegalArgumentException("Provided user is not OFFICER");
        }

        lead.setAssignedOfficer(officerUser);
        lead.setStatus(LeadStatus.SUBMITTED_TO_OFFICER);
        lead.setOfficerRemarks(safeTrim(request.getRemarks()));

        AgentLead saved = agentLeadRepository.save(lead);
        assistedAuditService.log(saved, null, lead.getAgent(), AssistedActionType.SUBMITTED_TO_OFFICER,
                "Lead submitted to officer", "officerUserId=" + officerUser.getId());
        return toResponse(saved);
    }

    private AgentLead requireOwnedLead(Long agentUserId, Long leadId) {
        return agentLeadRepository.findByIdAndAgentId(leadId, agentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found for this agent: " + leadId));
    }

    private String generateLeadCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return "LEAD-" + ts + "-" + suffix;
    }

    private String safeTrim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
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


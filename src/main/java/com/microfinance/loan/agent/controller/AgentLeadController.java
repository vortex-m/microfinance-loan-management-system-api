package com.microfinance.loan.agent.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.lead.dto.request.*;
import com.microfinance.loan.lead.dto.response.LeadResponse;
import com.microfinance.loan.lead.service.AgentLeadService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/agents/leads")
public class AgentLeadController {

    private final AgentLeadService agentLeadService;
    private final CurrentUserService currentUserService;

    public AgentLeadController(AgentLeadService agentLeadService, CurrentUserService currentUserService) {
        this.agentLeadService = agentLeadService;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping
    public ApiResponse<LeadResponse> createLead(Authentication auth, @Valid @RequestBody CreateLeadRequest request) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead created successfully", agentLeadService.createLead(agentUserId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/mine")
    public ApiResponse<List<LeadResponse>> getMyLeads(Authentication auth) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Leads fetched successfully", agentLeadService.getMyLeads(agentUserId));
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/{leadId}")
    public ApiResponse<LeadResponse> getMyLead(Authentication auth, @PathVariable Long leadId) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead fetched successfully", agentLeadService.getMyLead(agentUserId, leadId));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{leadId}/profile")
    public ApiResponse<LeadResponse> updateProfile(Authentication auth,
                                                   @PathVariable Long leadId,
                                                   @Valid @RequestBody LeadProfileUpdateRequest request) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead profile updated successfully", agentLeadService.updateProfile(agentUserId, leadId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping(value = "/{leadId}/kyc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<LeadResponse> uploadKyc(Authentication auth,
                                               @PathVariable Long leadId,
                                               @RequestParam KycDocumentType documentType,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead KYC uploaded successfully", agentLeadService.uploadKyc(agentUserId, leadId, documentType, file));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/{leadId}/consent")
    public ApiResponse<LeadResponse> captureConsent(Authentication auth,
                                                    @PathVariable Long leadId,
                                                    @Valid @RequestBody LeadConsentRequest request) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead consent captured successfully", agentLeadService.captureConsent(agentUserId, leadId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/{leadId}/loan-draft")
    public ApiResponse<LeadResponse> createLoanDraft(Authentication auth,
                                                     @PathVariable Long leadId,
                                                     @Valid @RequestBody LeadLoanDraftRequest request) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead loan draft saved successfully", agentLeadService.createLoanDraft(agentUserId, leadId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/{leadId}/submit-to-officer")
    public ApiResponse<LeadResponse> submitToOfficer(Authentication auth,
                                                     @PathVariable Long leadId,
                                                     @Valid @RequestBody SubmitLeadToOfficerRequest request) {
        Long agentUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead submitted to officer successfully", agentLeadService.submitToOfficer(agentUserId, leadId, request));
    }
}


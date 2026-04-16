package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.lead.dto.request.ConvertLeadToUserRequest;
import com.microfinance.loan.lead.dto.request.OfficerLeadDecisionRequest;
import com.microfinance.loan.lead.dto.response.LeadResponse;
import com.microfinance.loan.lead.service.OfficerLeadService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/officers/leads")
public class OfficerLeadController {

    private final OfficerLeadService officerLeadService;
    private final CurrentUserService currentUserService;

    public OfficerLeadController(OfficerLeadService officerLeadService, CurrentUserService currentUserService) {
        this.officerLeadService = officerLeadService;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/queue")
    public ApiResponse<List<LeadResponse>> getQueue(Authentication auth) {
        Long officerUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Officer lead queue fetched", officerLeadService.getQueue(officerUserId));
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping("/{leadId}/decision")
    public ApiResponse<LeadResponse> decide(Authentication auth,
                                            @PathVariable Long leadId,
                                            @Valid @RequestBody OfficerLeadDecisionRequest request) {
        Long officerUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead decision updated", officerLeadService.decide(officerUserId, leadId, request));
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping("/{leadId}/convert-user")
    public ApiResponse<LeadResponse> convert(Authentication auth,
                                             @PathVariable Long leadId,
                                             @Valid @RequestBody ConvertLeadToUserRequest request) {
        Long officerUserId = currentUserService.getCurrentUserId(auth);
        return ApiResponse.success("Lead converted to user successfully", officerLeadService.convertToUser(officerUserId, leadId, request));
    }
}


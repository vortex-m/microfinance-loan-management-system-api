package com.microfinance.loan.agent.controller;

import com.microfinance.loan.agent.dto.request.VerificationImageRequest;
import com.microfinance.loan.agent.dto.request.VerificationReportRequest;
import com.microfinance.loan.agent.dto.response.VerificationReportResponse;
import com.microfinance.loan.agent.service.AgentVerificationService;
import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/agents/tasks")
public class AgentVerificationController {

    private final AgentVerificationService agentVerificationService;
    private final CurrentUserService currentUserService;

    public AgentVerificationController(AgentVerificationService agentVerificationService,
                                       CurrentUserService currentUserService) {
        this.agentVerificationService = agentVerificationService;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/{taskId}/report")
    public ApiResponse<VerificationReportResponse> submitReport(Authentication authentication,
                                                                @PathVariable Long taskId,
                                                                @Valid @RequestBody VerificationReportRequest request) {
        Long agentId = currentUserService.getCurrentUserId(authentication);
        request.setTaskId(taskId);
        return ApiResponse.success("Verification report submitted",
                agentVerificationService.submitReport(agentId, taskId, request));
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/{taskId}/report")
    public ApiResponse<VerificationReportResponse> getReport(Authentication authentication,
                                                             @PathVariable Long taskId) {
        Long agentId = currentUserService.getCurrentUserId(authentication);
        return ApiResponse.success("Verification report fetched",
                agentVerificationService.getReport(agentId, taskId));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping(value = "/{taskId}/report/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VerificationReportResponse> uploadImage(Authentication authentication,
                                                               @PathVariable Long taskId,
                                                               @RequestParam("file") MultipartFile file,
                                                               @ModelAttribute VerificationImageRequest request) throws IOException {
        Long agentId = currentUserService.getCurrentUserId(authentication);
        return ApiResponse.success("Verification image uploaded",
                agentVerificationService.uploadImage(agentId, taskId, request, file));
    }
}

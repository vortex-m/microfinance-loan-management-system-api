package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.officer.dto.request.KycReviewDecisionRequest;
import com.microfinance.loan.officer.dto.response.KycReviewResponse;
import com.microfinance.loan.officer.service.OfficerKycService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/officers/kyc")
public class OfficerKycController {

	private final OfficerKycService officerKycService;

	public OfficerKycController(OfficerKycService officerKycService) {
		this.officerKycService = officerKycService;
	}

	@PreAuthorize("hasRole('OFFICER')")
	@GetMapping("/documents/pending")
	public ApiResponse<List<KycReviewResponse>> getPendingKycDocuments() {
		return ApiResponse.success("Pending KYC documents fetched", officerKycService.getPendingKycDocuments());
	}

	@PreAuthorize("hasRole('OFFICER')")
	@PostMapping("/users/{userId}/documents/{documentId}/review")
	public ApiResponse<KycReviewResponse> reviewUserKyc(
			@PathVariable Long userId,
			@PathVariable Long documentId,
			@Valid @RequestBody KycReviewDecisionRequest request
	) {
		return ApiResponse.success("KYC review updated", officerKycService.reviewUserKyc(userId, documentId, request));
	}
}

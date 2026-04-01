package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.enums.FileType;
import com.microfinance.loan.user.dto.request.KycUploadRequest;
import com.microfinance.loan.user.dto.response.KycStatusResponse;
import com.microfinance.loan.user.service.UserKycService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserKycService userKycService;

	public UserController(UserKycService userKycService) {
		this.userKycService = userKycService;
	}


	@PreAuthorize("hasRole('USER') and @userAccessGuard.canAccessUser(#userId, authentication)")
	@PostMapping(value = "/{userId}/kyc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<KycStatusResponse.KycDocumentItem> uploadKyc(
			@PathVariable Long userId,
			@RequestParam FileType documentType,
			@RequestParam @NotBlank String documentNumber,
			@RequestParam("file") MultipartFile file
	) throws IOException {
		KycUploadRequest request = KycUploadRequest.builder()
				.documentType(documentType)
				.documentNumber(documentNumber)
				.build();

		KycStatusResponse.KycDocumentItem item = userKycService.uploadKycDocument(userId, request, file);
		return ApiResponse.success("KYC document uploaded successfully", item);
	}

	@PreAuthorize("hasRole('USER') and @userAccessGuard.canAccessUser(#userId, authentication)")
	@GetMapping("/{userId}/kyc")
	public ApiResponse<KycStatusResponse> getKycStatus(@PathVariable Long userId) {
		return ApiResponse.success("KYC status fetched", userKycService.getKycStatus(userId));
	}
}

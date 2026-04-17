package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.request.KycUploadRequest;
import com.microfinance.loan.user.dto.response.KycStatusResponse;
import com.microfinance.loan.user.service.UserKycService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class UserKycController {
    private final UserKycService userKycService;
    private final CurrentUserService currentUserService;

    public UserKycController(UserKycService userKycService, CurrentUserService currentUserService) {
        this.userKycService = userKycService;
        this.currentUserService = currentUserService;
    }


    @PreAuthorize("hasRole('USER')")
      @PostMapping(value = "/kyc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KycStatusResponse.KycDocumentItem> uploadKyc(
            Authentication auth,
//            @PathVariable Long userId,
            @RequestParam KycDocumentType documentType,
            @RequestParam @NotBlank String documentNumber,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Long userId = currentUserService.getCurrentUserId(auth);

        KycUploadRequest request = KycUploadRequest.builder()
                .documentType(documentType)
                .documentNumber(documentNumber)
                .build();

        KycStatusResponse.KycDocumentItem item = userKycService.uploadKycDocument(userId, request, file);
        return ApiResponse.success("KYC document uploaded successfully", item);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/kyc/submit")
    public ApiResponse<KycStatusResponse> submitKyc(Authentication auth) {
        Long userId = currentUserService.getCurrentUserId(auth);

        return ApiResponse.success("KYC submitted for review", userKycService.submitKyc(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/kyc/{documentId}")
    public ApiResponse<KycStatusResponse> deletePendingKyc(Authentication auth, @PathVariable Long documentId) {
        Long userId = currentUserService.getCurrentUserId(auth);

        return ApiResponse.success(
                "Pending KYC document deleted successfully",
                userKycService.deletePendingKycDocument(userId, documentId)
        );
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping(value = "/kyc/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KycStatusResponse.KycDocumentItem> editPendingKyc(Authentication auth,
                                                                         @PathVariable Long documentId,
                                                                         @RequestParam(required = false) String documentNumber,
                                                                         @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        Long userId = currentUserService.getCurrentUserId(auth);

        KycStatusResponse.KycDocumentItem item = userKycService
                .editPendingKycDocument(userId, documentId, documentNumber, file);
        return ApiResponse.success("Pending KYC document updated successfully", item);
    }

    @PreAuthorize("hasRole('USER')")
      @GetMapping("/kyc")
    public ApiResponse<KycStatusResponse> getKycStatus(Authentication auth) {
        Long userId = currentUserService.getCurrentUserId(auth);

        return ApiResponse.success("KYC status fetched", userKycService.getKycStatus(userId));
    }
}

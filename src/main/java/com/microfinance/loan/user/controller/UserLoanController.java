package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.user.dto.request.LoanApplyRequest;
import com.microfinance.loan.user.dto.response.BankProofUploadResponse;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.service.UserLoanService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class UserLoanController {

	private final UserLoanService userLoanService;

	public UserLoanController(UserLoanService userLoanService) {
		this.userLoanService = userLoanService;
	}

	@PreAuthorize("hasRole('USER') and @userAccessGuard.canAccessUser(#userId, authentication)")
	@PostMapping(value = "/{userId}/loans/bank-proof/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<BankProofUploadResponse> uploadBankProof(
			@PathVariable Long userId,
			@RequestParam("file") MultipartFile file
	) throws IOException {
		return ApiResponse.success("Bank proof uploaded successfully", userLoanService.uploadBankProof(userId, file));
	}

	@PreAuthorize("hasRole('USER') and @userAccessGuard.canAccessUser(#userId, authentication)")
	@PostMapping("/{userId}/loans/apply")
	public ApiResponse<LoanApplyResponse> applyForLoan(
			@PathVariable Long userId,
			@Valid @RequestBody LoanApplyRequest request
	) {
		LoanApplyResponse response = userLoanService.applyForLoan(userId, request);
		return ApiResponse.success("Loan application submitted successfully", response);
	}
}

package com.microfinance.loan.user.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.user.dto.request.LoanApplyRequest;
import com.microfinance.loan.user.dto.response.BankProofUploadResponse;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.service.UserLoanService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
	private final CurrentUserService currentUserService;

	public UserLoanController(UserLoanService userLoanService, CurrentUserService currentUserService) {
		this.userLoanService = userLoanService;
		this.currentUserService = currentUserService;
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping(value = "/loans/bank-proof/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<BankProofUploadResponse> uploadBankProof(
			Authentication auth,
			@RequestParam("file") MultipartFile file
	) throws IOException {
		Long userId = currentUserService.getCurrentUserId(auth);

		return ApiResponse.success("Bank proof uploaded successfully", userLoanService.uploadBankProof(userId, file));
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/loans/apply")
	public ApiResponse<LoanApplyResponse> applyForLoan(
			Authentication auth,
			@Valid @RequestBody LoanApplyRequest request
	) {
		Long userId = currentUserService.getCurrentUserId(auth);

		LoanApplyResponse response = userLoanService.applyForLoan(userId, request);
		return ApiResponse.success("Loan application submitted successfully", response);
	}
}

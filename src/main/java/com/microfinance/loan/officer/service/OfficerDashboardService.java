package com.microfinance.loan.officer.service;

import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.ReviewDecision;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.officer.dto.response.OfficerDashboardResponse;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.LoanReviewRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficerDashboardService {

	private final OfficerProfileRepository officerProfileRepository;
	private final CurrentUserService currentUserService;
	private final KycDocumentRepository kycDocumentRepository;
	private final LoanApplicationRepository loanApplicationRepository;
	private final LoanReviewRepository loanReviewRepository;
	private final UserProfileRepository userProfileRepository;

	public OfficerDashboardService(OfficerProfileRepository officerProfileRepository,
								   CurrentUserService currentUserService,
								   KycDocumentRepository kycDocumentRepository,
								   LoanApplicationRepository loanApplicationRepository,
								   LoanReviewRepository loanReviewRepository,
								   UserProfileRepository userProfileRepository) {
		this.officerProfileRepository = officerProfileRepository;
		this.currentUserService = currentUserService;
		this.kycDocumentRepository = kycDocumentRepository;
		this.loanApplicationRepository = loanApplicationRepository;
		this.loanReviewRepository = loanReviewRepository;
		this.userProfileRepository = userProfileRepository;
	}

	public OfficerDashboardResponse getDashboard(org.springframework.security.core.Authentication authentication) {
		Long officerUserId = currentUserService.getCurrentUserId(authentication);
		OfficerProfile officer = officerProfileRepository.findByUsersIdWithBranch(officerUserId)
				.orElseThrow(() -> new IllegalArgumentException("Officer profile not found for user: " + officerUserId));

		if (officer.getBranchProfile() == null) {
			throw new IllegalArgumentException("Officer is not mapped to any branch");
		}

		String branchCode = officer.getBranchProfile().getBranchCode();
		long pendingKyc = kycDocumentRepository.findByVerificationStatusInOrderByCreatedAtAsc(List.of(KycStatus.PENDING, KycStatus.IN_REVIEW))
				.stream()
				.filter(doc -> doc.getUser() != null
						&& doc.getUser().getId() != null
						&& userProfileRepository.findByUsersId(doc.getUser().getId())
						.map(up -> up.getBranchProfile() != null
								&& branchCode.equalsIgnoreCase(up.getBranchProfile().getBranchCode()))
						.orElse(false))
				.count();

		long pendingLoans = loanApplicationRepository.countByBranchCodeAndStatuses(
				branchCode,
				List.of(LoanStatus.PENDING, LoanStatus.UNDER_REVIEW)
		);

		return OfficerDashboardResponse.builder()
				.officerUserId(officerUserId)
				.officerCode(officer.getOfficerCode())
				.branchCode(branchCode)
				.pendingKycDocuments(pendingKyc)
				.pendingLoanQueue(pendingLoans)
				.reviewsApproved(loanReviewRepository.countByOfficerIdAndDecision(officerUserId, ReviewDecision.APPROVED))
				.reviewsRejected(loanReviewRepository.countByOfficerIdAndDecision(officerUserId, ReviewDecision.REJECTED))
				.reviewsReverify(loanReviewRepository.countByOfficerIdAndDecision(officerUserId, ReviewDecision.RE_VERIFY))
				.build();
	}
}


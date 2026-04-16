package com.microfinance.loan.officer.repository;

import com.microfinance.loan.common.enums.ReviewDecision;
import com.microfinance.loan.officer.entity.LoanReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanReviewRepository extends JpaRepository<LoanReview, Long> {
	Optional<LoanReview> findByLoanApplicationId(Long loanApplicationId);

	List<LoanReview> findByOfficerIdOrderByUpdatedAtDesc(Long officerId);

	long countByOfficerIdAndDecision(Long officerId, ReviewDecision decision);
}

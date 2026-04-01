package com.microfinance.loan.officer.repository;

import com.microfinance.loan.officer.entity.LoanReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanReviewRepository extends JpaRepository<LoanReview, Long> {
}

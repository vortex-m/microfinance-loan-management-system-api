package com.microfinance.loan.officer.repository;

import com.microfinance.loan.officer.entity.KycReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycReviewRepository extends JpaRepository<KycReview, Long> {
}

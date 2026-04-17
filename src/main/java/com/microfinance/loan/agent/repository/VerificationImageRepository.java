package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.VerificationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationImageRepository extends JpaRepository<VerificationImage, Long> {
	int countByVerificationReportId(Long verificationReportId);

	List<VerificationImage> findByVerificationReportIdOrderByCreatedAtDesc(Long verificationReportId);
}

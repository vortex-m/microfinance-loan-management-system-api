package com.microfinance.loan.user.repository;

import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.user.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
	List<KycDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
	List<KycDocument> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
	Optional<KycDocument> findTopByUserIdAndDocumentTypeAndIsActiveTrueOrderByVersionDesc(Long userId, KycDocumentType documentType);
	boolean existsByUserIdAndDocumentTypeAndIsActiveTrue(Long userId, KycDocumentType documentType);
	boolean existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(Long userId, KycDocumentType documentType, KycStatus verificationStatus);
	List<KycDocument> findByVerificationStatusInOrderByCreatedAtAsc(List<KycStatus> statuses);
}

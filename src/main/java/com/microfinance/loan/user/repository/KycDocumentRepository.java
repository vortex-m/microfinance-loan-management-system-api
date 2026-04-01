package com.microfinance.loan.user.repository;

import com.microfinance.loan.user.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
	List<KycDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
}

package com.microfinance.loan.user.repository;

import com.microfinance.loan.user.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycRepository extends JpaRepository<KycDocument, Long> {
}

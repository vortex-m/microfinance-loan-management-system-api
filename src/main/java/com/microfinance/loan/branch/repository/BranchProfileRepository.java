package com.microfinance.loan.branch.repository;

import com.microfinance.loan.branch.entity.BranchProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchProfileRepository extends JpaRepository<BranchProfile, Long> {
    Optional<BranchProfile> findByBranchCodeIgnoreCase(String branchCode);

    boolean existsByBranchCodeIgnoreCase(String branchCode);
}


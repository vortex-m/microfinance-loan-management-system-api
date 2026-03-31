package com.microfinance.loan.officer.repository;

import com.microfinance.loan.officer.entity.OfficerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerProfileRepository extends JpaRepository<OfficerProfile, Long> {
    Optional<OfficerProfile> findByOfficerCode(String officerCode);

    boolean existsByOfficerCode(String officerCode);
}


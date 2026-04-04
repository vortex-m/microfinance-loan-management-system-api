package com.microfinance.loan.officer.repository;

import com.microfinance.loan.officer.entity.OfficerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerProfileRepository extends JpaRepository<OfficerProfile, Long> {
    Optional<OfficerProfile> findByOfficerCode(String officerCode);

    @Query("select op from OfficerProfile op join fetch op.users where op.officerCode = :officerCode")
    Optional<OfficerProfile> findByOfficerCodeWithUsers(@Param("officerCode") String officerCode);

    Optional<OfficerProfile> findByUsersId(Long userId);

    boolean existsByOfficerCode(String officerCode);
    boolean existsByUsersId(Long userId);
}


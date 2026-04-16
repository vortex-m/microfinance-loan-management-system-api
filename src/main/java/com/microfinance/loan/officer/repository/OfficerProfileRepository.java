package com.microfinance.loan.officer.repository;

import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.officer.entity.OfficerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficerProfileRepository extends JpaRepository<OfficerProfile, Long> {
    Optional<OfficerProfile> findByOfficerCode(String officerCode);

    @Query("select op from OfficerProfile op join fetch op.users where op.officerCode = :officerCode")
    Optional<OfficerProfile> findByOfficerCodeWithUsers(@Param("officerCode") String officerCode);

    @Query("select op from OfficerProfile op join fetch op.users left join fetch op.branchProfile where op.users.id = :userId")
    Optional<OfficerProfile> findByUsersIdWithBranch(@Param("userId") Long userId);

    Optional<OfficerProfile> findByUsersId(Long userId);

    @Query("select op from OfficerProfile op join fetch op.users u join fetch op.branchProfile bp " +
            "where bp.branchCode = :branchCode and op.officerStatus = :status")
    List<OfficerProfile> findActiveByBranchCode(@Param("branchCode") String branchCode,
                                                @Param("status") OfficerStatus status);

    boolean existsByOfficerCode(String officerCode);
    boolean existsByUsersId(Long userId);

    long countByBranchProfileBranchCode(String branchCode);

    long countByBranchProfileBranchCodeAndOfficerStatus(String branchCode, OfficerStatus officerStatus);
}


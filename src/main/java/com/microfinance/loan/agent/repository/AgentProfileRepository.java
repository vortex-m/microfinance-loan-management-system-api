package com.microfinance.loan.agent.repository;

import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.agent.entity.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentProfileRepository extends JpaRepository<AgentProfile, Long> {
    Optional<AgentProfile> findByAgentCode(String agentCode);

    @Query("select ap from AgentProfile ap join fetch ap.users where ap.agentCode = :agentCode")
    Optional<AgentProfile> findByAgentCodeWithUsers(@Param("agentCode") String agentCode);

    @Query("select ap from AgentProfile ap join fetch ap.users left join fetch ap.branchProfile where ap.users.id = :userId")
    Optional<AgentProfile> findByUsersIdWithBranch(@Param("userId") Long userId);

    Optional<AgentProfile> findByUsersId(Long userId);

    @Query("select ap from AgentProfile ap join fetch ap.users u join fetch ap.branchProfile bp " +
            "where bp.branchCode = :branchCode and ap.agentStatus = :status and ap.agentAvailability = :availability")
    List<AgentProfile> findActiveAvailableByBranchCode(@Param("branchCode") String branchCode,
                                                       @Param("status") AgentStatus status,
                                                       @Param("availability") AgentAvailability availability);

    boolean existsByAgentCode(String agentCode);
    boolean existsByUsersId(Long userId);

    long countByBranchProfileBranchCode(String branchCode);

    long countByBranchProfileBranchCodeAndAgentStatus(String branchCode, AgentStatus agentStatus);
}


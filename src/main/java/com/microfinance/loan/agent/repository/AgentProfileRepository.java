package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentProfileRepository extends JpaRepository<AgentProfile, Long> {
    Optional<AgentProfile> findByAgentCode(String agentCode);

    @Query("select ap from AgentProfile ap join fetch ap.users where ap.agentCode = :agentCode")
    Optional<AgentProfile> findByAgentCodeWithUsers(@Param("agentCode") String agentCode);

    Optional<AgentProfile> findByUsersId(Long userId);

    boolean existsByAgentCode(String agentCode);
    boolean existsByUsersId(Long userId);
}


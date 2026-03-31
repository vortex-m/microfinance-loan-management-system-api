package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentProfileRepository extends JpaRepository<AgentProfile, Long> {
    Optional<AgentProfile> findByAgentCode(String agentCode);

    boolean existsByAgentCode(String agentCode);
}


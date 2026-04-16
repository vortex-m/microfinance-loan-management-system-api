package com.microfinance.loan.lead.repository;

import com.microfinance.loan.common.enums.LeadStatus;
import com.microfinance.loan.lead.entity.AgentLead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AgentLeadRepository extends JpaRepository<AgentLead, Long> {
    List<AgentLead> findByAgentIdOrderByCreatedAtDesc(Long agentId);

    Optional<AgentLead> findByIdAndAgentId(Long id, Long agentId);

    List<AgentLead> findByAssignedOfficerIdAndStatusInOrderByCreatedAtAsc(Long assignedOfficerId, Collection<LeadStatus> statuses);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    boolean existsByPanNumber(String panNumber);

    Optional<AgentLead> findByLeadCode(String leadCode);
}


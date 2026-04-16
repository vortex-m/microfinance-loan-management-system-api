package com.microfinance.loan.lead.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AssistedActionType;
import com.microfinance.loan.lead.entity.AgentLead;
import com.microfinance.loan.lead.entity.AssistedActionAudit;
import com.microfinance.loan.lead.repository.AssistedActionAuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AssistedAuditService {

    private final AssistedActionAuditRepository assistedActionAuditRepository;

    public AssistedAuditService(AssistedActionAuditRepository assistedActionAuditRepository) {
        this.assistedActionAuditRepository = assistedActionAuditRepository;
    }

    public void log(AgentLead lead,
                    Users user,
                    Users performedBy,
                    AssistedActionType actionType,
                    String remarks,
                    String metadata) {
        AssistedActionAudit audit = AssistedActionAudit.builder()
                .auditCode(generateAuditCode())
                .lead(lead)
                .user(user)
                .performedBy(performedBy)
                .actionType(actionType)
                .remarks(remarks)
                .metadata(metadata)
                .build();
        assistedActionAuditRepository.save(audit);
    }

    public List<AssistedActionAudit> getLatest() {
        return assistedActionAuditRepository.findTop100ByOrderByCreatedAtDesc();
    }

    private String generateAuditCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ASST-" + ts + "-" + suffix;
    }
}


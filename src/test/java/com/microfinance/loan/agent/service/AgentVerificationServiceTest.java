package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.VerificationReportRequest;
import com.microfinance.loan.agent.dto.response.VerificationReportResponse;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.entity.VerificationReport;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.VerificationImageRepository;
import com.microfinance.loan.agent.repository.VerificationReportRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.VerificationStatus;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.user.entity.LoanApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentVerificationServiceTest {

    @Mock
    private AgentTaskRepository agentTaskRepository;
    @Mock
    private VerificationReportRepository verificationReportRepository;
    @Mock
    private VerificationImageRepository verificationImageRepository;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AgentVerificationService agentVerificationService;

    @Test
    void submitReport_shouldCreateVerifiedReportForGoodInput() {
        LoanApplication application = LoanApplication.builder().id(91L).applicationNumber("APP-1001").build();
        Users agent = Users.builder().id(101L).build();
        AgentTask task = AgentTask.builder()
                .id(7L)
                .loanApplication(application)
                .agent(agent)
                .taskStatus(TaskStatus.ASSIGNED)
                .build();

        when(agentTaskRepository.findByIdAndAgentId(7L, 101L)).thenReturn(Optional.of(task));
        when(verificationReportRepository.findByTaskIdAndAgentId(7L, 101L)).thenReturn(Optional.empty());
        when(agentTaskRepository.save(any(AgentTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(verificationReportRepository.save(any(VerificationReport.class))).thenAnswer(invocation -> {
            VerificationReport report = invocation.getArgument(0);
            report.setId(501L);
            return report;
        });
        when(verificationImageRepository.countByVerificationReportId(501L)).thenReturn(0);

        VerificationReportRequest request = VerificationReportRequest.builder()
                .taskId(7L)
                .reportSummary("Applicant available and documents verified")
                .documentsMatched(true)
                .applicantAvailable(true)
                .suspiciousActivity(false)
                .build();

        VerificationReportResponse response = agentVerificationService.submitReport(101L, 7L, request);

        assertEquals(501L, response.getReportId());
        assertEquals(VerificationStatus.VERIFIED, response.getVerificationStatus());
        assertEquals(true, response.getDocumentsMatched());
    }
}


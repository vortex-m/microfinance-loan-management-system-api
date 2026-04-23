package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.VerificationImageRequest;
import com.microfinance.loan.agent.dto.request.VerificationReportRequest;
import com.microfinance.loan.agent.dto.request.VerificationSubmissionRequest;
import com.microfinance.loan.agent.dto.response.AgentTaskResponse;
import com.microfinance.loan.agent.dto.response.VerificationReportResponse;
import com.microfinance.loan.agent.dto.response.VerificationSubmissionResponse;
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
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
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
    @Mock
    private AgentTaskService agentTaskService;

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

    @Test
    void submitVerification_shouldSubmitReportUploadImagesAndCompleteTask() throws Exception {
        LoanApplication application = LoanApplication.builder().id(91L).applicationNumber("APP-1001").build();
        Users agent = Users.builder().id(101L).build();
        AgentTask task = AgentTask.builder()
                .id(7L)
                .loanApplication(application)
                .agent(agent)
                .taskStatus(TaskStatus.ACCEPTED)
                .build();
        VerificationReport persistedReport = VerificationReport.builder()
                .id(501L)
                .task(task)
                .loanApplication(application)
                .agent(agent)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        when(agentTaskRepository.findByIdAndAgentId(7L, 101L)).thenReturn(Optional.of(task));
        when(verificationReportRepository.findByTaskIdAndAgentId(7L, 101L))
                .thenReturn(Optional.empty(), Optional.of(persistedReport), Optional.of(persistedReport), Optional.of(persistedReport));
        when(agentTaskRepository.save(any(AgentTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(verificationReportRepository.save(any(VerificationReport.class))).thenAnswer(invocation -> {
            VerificationReport report = invocation.getArgument(0);
            report.setId(501L);
            return report;
        });
        when(fileStorageService.storeFile(any(), any())).thenReturn("upload/test.jpg");
        when(verificationImageRepository.countByVerificationReportId(501L)).thenReturn(0, 2);
        when(agentTaskService.completeTask(101L, 7L)).thenReturn(
                AgentTaskResponse.builder()
                        .taskId(7L)
                        .taskStatus(TaskStatus.COMPLETED)
                        .completedAt(LocalDateTime.now())
                        .build()
        );

        VerificationSubmissionRequest request = VerificationSubmissionRequest.builder()
                .report(VerificationReportRequest.builder()
                        .taskId(7L)
                        .reportSummary("Verified")
                        .documentsMatched(true)
                        .applicantAvailable(true)
                        .suspiciousActivity(false)
                        .build())
                .images(List.of(
                        VerificationImageRequest.builder()
                                .imageTag("FRONT")
                                .description("Front view")
                                .captureLatitude(20.1)
                                .captureLongitude(80.2)
                                .build(),
                        VerificationImageRequest.builder()
                                .imageTag("SELFIE")
                                .description("Applicant selfie")
                                .captureLatitude(20.1)
                                .captureLongitude(80.2)
                                .build()
                ))
                .build();

        List<MockMultipartFile> files = List.of(
                new MockMultipartFile("files", "front.jpg", "image/jpeg", "a".getBytes()),
                new MockMultipartFile("files", "selfie.jpg", "image/jpeg", "b".getBytes())
        );

        VerificationSubmissionResponse response = agentVerificationService.submitVerification(101L, 7L, request, List.copyOf(files));

        assertEquals(501L, response.getReportId());
        assertEquals(TaskStatus.COMPLETED, response.getTaskStatus());
        assertEquals(2, response.getImageCount());
    }
}


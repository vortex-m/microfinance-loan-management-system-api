package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.VerificationImageRequest;
import com.microfinance.loan.agent.dto.request.VerificationReportRequest;
import com.microfinance.loan.agent.dto.request.VerificationSubmissionRequest;
import com.microfinance.loan.agent.dto.response.AgentTaskResponse;
import com.microfinance.loan.agent.dto.response.VerificationReportResponse;
import com.microfinance.loan.agent.dto.response.VerificationSubmissionResponse;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.entity.VerificationImage;
import com.microfinance.loan.agent.entity.VerificationReport;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.VerificationImageRepository;
import com.microfinance.loan.agent.repository.VerificationReportRepository;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.VerificationStatus;
import com.microfinance.loan.common.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AgentVerificationService {

    private final AgentTaskRepository agentTaskRepository;
    private final VerificationReportRepository verificationReportRepository;
    private final VerificationImageRepository verificationImageRepository;
    private final FileStorageService fileStorageService;
    private final AgentTaskService agentTaskService;

    public AgentVerificationService(AgentTaskRepository agentTaskRepository,
                                    VerificationReportRepository verificationReportRepository,
                                    VerificationImageRepository verificationImageRepository,
                                    FileStorageService fileStorageService,
                                    AgentTaskService agentTaskService) {
        this.agentTaskRepository = agentTaskRepository;
        this.verificationReportRepository = verificationReportRepository;
        this.verificationImageRepository = verificationImageRepository;
        this.fileStorageService = fileStorageService;
        this.agentTaskService = agentTaskService;
    }

    @Transactional
    public VerificationSubmissionResponse submitVerification(Long agentId,
                                                             Long taskId,
                                                             VerificationSubmissionRequest request,
                                                             List<MultipartFile> files) throws IOException {
        if (request == null || request.getReport() == null) {
            throw new IllegalArgumentException("Verification report payload is required");
        }
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new IllegalArgumentException("At least one image metadata entry is required");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one verification image file is required");
        }
        if (files.size() != request.getImages().size()) {
            throw new IllegalArgumentException("Image metadata count must match uploaded files count");
        }

        request.getReport().setTaskId(taskId);
        submitReport(agentId, taskId, request.getReport());

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Image file at index " + i + " is empty");
            }
            uploadImage(agentId, taskId, request.getImages().get(i), file);
        }

        AgentTaskResponse taskResponse = agentTaskService.completeTask(agentId, taskId);
        VerificationReportResponse finalReport = getReport(agentId, taskId);

        return VerificationSubmissionResponse.builder()
                .taskId(taskResponse.getTaskId())
                .taskStatus(taskResponse.getTaskStatus())
                .reportId(finalReport.getReportId())
                .verificationStatus(finalReport.getVerificationStatus())
                .imageCount(finalReport.getImageCount())
                .completedAt(taskResponse.getCompletedAt())
                .build();
    }

    @Transactional
    public VerificationReportResponse submitReport(Long agentId, Long taskId, VerificationReportRequest request) {
        AgentTask task = getAgentTask(agentId, taskId);

        if (task.getTaskStatus() == TaskStatus.DECLINED || task.getTaskStatus() == TaskStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot submit report for task in status: " + task.getTaskStatus());
        }

        VerificationReport report = verificationReportRepository.findByTaskIdAndAgentId(taskId, agentId)
                .orElseGet(() -> VerificationReport.builder()
                        .reportCode(generateReportCode())
                        .task(task)
                        .loanApplication(task.getLoanApplication())
                        .agent(task.getAgent())
                        .visitedAt(LocalDateTime.now())
                        .build());

        report.setAgentRemarks(request.getReportSummary());
        report.setEnvironmentObservation(request.getResidenceRemarks());
        report.setBusinessObservation(request.getBusinessRemarks());
        report.setAddressVerified(request.getAddressVerified());
        report.setIncomeVerified(request.getIncomeVerified());
        report.setApplicantPresent(request.getApplicantAvailable());
        report.setVisitLatitude(request.getVisitLatitude());
        report.setVisitLongitude(request.getVisitLongitude());
        report.setVisitAddress(request.getVisitAddress());
        report.setDocumentTampering(request.getDocumentsMatched() == null ? null : !request.getDocumentsMatched());
        report.setSuspiciousActivity(Boolean.TRUE.equals(request.getSuspiciousActivity()));
        report.setRiskNotes(request.getRiskNotes());
        report.setCashCollectedAmount(request.getCashCollectedAmount());
        report.setCashCollectionRemarks(request.getCashCollectionRemarks());
        report.setVerificationStatus(resolveStatus(request));
        report.setSubmittedAt(LocalDateTime.now());
        report.setIsSubmitted(true);

        if (task.getTaskStatus() == TaskStatus.ASSIGNED || task.getTaskStatus() == TaskStatus.ACCEPTED) {
            task.setTaskStatus(TaskStatus.IN_PROGRESS);
            task.setStartedAt(LocalDateTime.now());
            agentTaskRepository.save(task);
        }

        VerificationReport saved = verificationReportRepository.save(report);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VerificationReportResponse getReport(Long agentId, Long taskId) {
        VerificationReport report = verificationReportRepository.findByTaskIdAndAgentId(taskId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("Verification report not found for task: " + taskId));
        return toResponse(report);
    }

    @Transactional
    public VerificationReportResponse uploadImage(Long agentId,
                                                  Long taskId,
                                                  VerificationImageRequest request,
                                                  MultipartFile file) throws IOException {
        VerificationReport report = verificationReportRepository.findByTaskIdAndAgentId(taskId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("Submit report before uploading images for task: " + taskId));

        String fileUrl = fileStorageService.storeFile(file, "verification/" + taskId);

        VerificationImage image = VerificationImage.builder()
                .verificationReport(report)
                .agent(report.getAgent())
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(String.valueOf(file.getSize()))
                .imageTag(request != null ? request.getImageTag() : null)
                .description(request != null ? request.getDescription() : null)
                .captureLatitude(request != null ? request.getCaptureLatitude() : null)
                .captureLongitude(request != null ? request.getCaptureLongitude() : null)
                .capturedAt(LocalDateTime.now())
                .build();

        verificationImageRepository.save(image);
        return toResponse(report);
    }

    private AgentTask getAgentTask(Long agentId, Long taskId) {
        return agentTaskRepository.findByIdAndAgentId(taskId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found for this agent: " + taskId));
    }

    private VerificationStatus resolveStatus(VerificationReportRequest request) {
        if (Boolean.TRUE.equals(request.getSuspiciousActivity())) {
            return VerificationStatus.SUSPICIOUS;
        }
        if (Boolean.FALSE.equals(request.getDocumentsMatched()) || Boolean.FALSE.equals(request.getApplicantAvailable())) {
            return VerificationStatus.FAILED;
        }
        return VerificationStatus.VERIFIED;
    }

    private VerificationReportResponse toResponse(VerificationReport report) {
        return VerificationReportResponse.builder()
                .reportId(report.getId())
                .taskId(report.getTask() != null ? report.getTask().getId() : null)
                .loanApplicationId(report.getLoanApplication() != null ? report.getLoanApplication().getId() : null)
                .reportSummary(report.getAgentRemarks())
                .documentsMatched(report.getDocumentTampering() == null ? null : !report.getDocumentTampering())
                .applicantAvailable(report.getApplicantPresent())
                .verificationStatus(report.getVerificationStatus())
                .imageCount(report.getId() == null ? 0 : verificationImageRepository.countByVerificationReportId(report.getId()))
                .cashCollectedAmount(report.getCashCollectedAmount())
                .cashCollectionRemarks(report.getCashCollectionRemarks())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private String generateReportCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "VR-" + ts + "-" + suffix;
    }
}

package com.microfinance.loan.manager.service;

import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.enums.ReportStatus;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.manager.dto.request.GenerateSystemReportRequest;
import com.microfinance.loan.manager.dto.response.SystemReportResponse;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.entity.SystemReport;
import com.microfinance.loan.manager.repository.FraudAlertRepository;
import com.microfinance.loan.manager.repository.SystemReportRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class SystemReportService {

    private final SystemReportRepository systemReportRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserProfileRepository userProfileRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final LoanRepository loanRepository;
    private final ManagerAuthorizationService managerAuthorizationService;

    public SystemReportService(SystemReportRepository systemReportRepository,
                               LoanApplicationRepository loanApplicationRepository,
                               UserProfileRepository userProfileRepository,
                               FraudAlertRepository fraudAlertRepository,
                               LoanRepository loanRepository,
                               ManagerAuthorizationService managerAuthorizationService) {
        this.systemReportRepository = systemReportRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userProfileRepository = userProfileRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.loanRepository = loanRepository;
        this.managerAuthorizationService = managerAuthorizationService;
    }

    @Transactional
    public SystemReportResponse generate(Authentication authentication, GenerateSystemReportRequest request) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        managerAuthorizationService.requireCanExportReports(managerProfile);
        managerAuthorizationService.requireDepartment(managerProfile,
                ManagerDepartment.BRANCH_OPERATIONS,
                ManagerDepartment.LOAN_OPERATIONS,
                ManagerDepartment.AUDIT_FRAUD_CONTROL,
                ManagerDepartment.COLLECTIONS_RECOVERY,
                ManagerDepartment.CREDIT_UNDERWRITING,
                ManagerDepartment.KYC_COMPLIANCE);

        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        long totalApplied = loanApplicationRepository.countByBranchCode(branchCode);
        long approved = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.APPROVED);
        long rejected = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.REJECTED);
        long disbursed = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.DISBURSED);
        long closed = loanApplicationRepository.countByBranchCodeAndStatus(branchCode, LoanStatus.CLOSED);

        double totalDisbursedAmount = defaultZero(loanApplicationRepository.sumTotalAmountByBranchCode(branchCode));
        double totalRepaidAmount = defaultZero(loanRepository.sumTotalPaidAmountByBranchCode(branchCode));

        long totalFraudAlerts = fraudAlertRepository.countByBranchCode(branchCode);
        long activeUsers = userProfileRepository.countByBranchCode(branchCode);

        long totalDisbursedOrClosed = disbursed + closed;
        long npaCount = loanRepository.countNpaByBranchCode(branchCode);
        double defaultRate = totalDisbursedOrClosed == 0 ? 0d : round((npaCount * 100.0d) / totalDisbursedOrClosed);

        LocalDateTime now = LocalDateTime.now();

        SystemReport report = SystemReport.builder()
                .reportCode(generateReportCode())
                .generatedBy(managerProfile.getUsers())
                .reportType(normalizeReportType(request.getReportType()))
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .reportData(buildSnapshotJson(totalApplied, approved, rejected, disbursed, closed,
                        totalDisbursedAmount, totalRepaidAmount, totalFraudAlerts, activeUsers, defaultRate))
                .totalLoansApplied(toInt(totalApplied))
                .totalLoansApproved(toInt(approved))
                .totalLoansRejected(toInt(rejected))
                .totalLoansDisbursed(toInt(disbursed))
                .totalLoansClosed(toInt(closed))
                .totalDisbursedAmount(totalDisbursedAmount)
                .totalRepaidAmount(totalRepaidAmount)
                .defaultRate(defaultRate)
                .totalFraudAlerts(toInt(totalFraudAlerts))
                .totalActiveUsers(toInt(activeUsers))
                .reportStatus(ReportStatus.READY)
                .exportFormat(normalizeExportFormat(request.getExportFormat()))
                .generatedAt(now)
                .build();

        SystemReport saved = systemReportRepository.save(report);

        managerProfile.setLastReportGeneratedAt(now);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SystemReportResponse> getMyBranchReports(Authentication authentication) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        return systemReportRepository.findByBranchCodeOrderByCreatedAtDesc(branchCode)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemReportResponse getById(Authentication authentication, Long reportId) {
        ManagerProfile managerProfile = managerAuthorizationService.getManagerWithBranch(authentication);
        String branchCode = managerProfile.getBranchProfile().getBranchCode();

        SystemReport report = systemReportRepository.findByIdAndBranchCode(reportId, branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Report not found for branch: " + reportId));

        return toResponse(report);
    }

    private SystemReportResponse toResponse(SystemReport report) {
        return SystemReportResponse.builder()
                .reportId(report.getId())
                .reportCode(report.getReportCode())
                .reportType(report.getReportType())
                .reportStatus(report.getReportStatus() != null ? report.getReportStatus().name() : null)
                .exportFileUrl(report.getExportFileUrl())
                .generatedAt(report.getGeneratedAt())
                .build();
    }

    private String buildSnapshotJson(long totalApplied,
                                     long approved,
                                     long rejected,
                                     long disbursed,
                                     long closed,
                                     double totalDisbursedAmount,
                                     double totalRepaidAmount,
                                     long totalFraudAlerts,
                                     long activeUsers,
                                     double defaultRate) {
        return "{" +
                "\"totalApplied\":" + totalApplied + "," +
                "\"approved\":" + approved + "," +
                "\"rejected\":" + rejected + "," +
                "\"disbursed\":" + disbursed + "," +
                "\"closed\":" + closed + "," +
                "\"totalDisbursedAmount\":" + totalDisbursedAmount + "," +
                "\"totalRepaidAmount\":" + totalRepaidAmount + "," +
                "\"totalFraudAlerts\":" + totalFraudAlerts + "," +
                "\"activeUsers\":" + activeUsers + "," +
                "\"defaultRate\":" + defaultRate +
                "}";
    }

    private String generateReportCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "RPT-" + ts + "-" + suffix;
    }

    private String normalizeReportType(String reportType) {
        if (!StringUtils.hasText(reportType)) {
            return "CUSTOM";
        }
        return reportType.trim().toUpperCase();
    }

    private String normalizeExportFormat(String exportFormat) {
        if (!StringUtils.hasText(exportFormat)) {
            return "JSON";
        }
        return exportFormat.trim().toUpperCase();
    }

    private int toInt(long value) {
        return Math.toIntExact(value);
    }

    private double defaultZero(Double value) {
        return value == null ? 0d : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0d) / 100.0d;
    }
}

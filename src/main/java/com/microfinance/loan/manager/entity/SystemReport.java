package com.microfinance.loan.manager.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reportCode;          // e.g. RPT-00123

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private Users generatedBy;          // manager who generated

    // Report Type & Period
    @Column(nullable = false)
    private String reportType;          // MONTHLY, QUARTERLY, CUSTOM, ANNUAL

    private LocalDate fromDate;
    private LocalDate toDate;

    // Report Content (stored as JSON string)
    @Column(columnDefinition = "TEXT")
    private String reportData;          // JSON snapshot of metrics

    // Key Metrics Snapshot
    private Integer totalLoansApplied;
    private Integer totalLoansApproved;
    private Integer totalLoansRejected;
    private Integer totalLoansDisbursed;
    private Integer totalLoansClosed;
    private Double totalDisbursedAmount;
    private Double totalRepaidAmount;
    private Double defaultRate;         // % of loans defaulted
    private Integer totalFraudAlerts;
    private Integer totalActiveUsers;

    // Export Info
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;  // GENERATING, READY, FAILED

    private String exportFileUrl;       // S3 URL of exported PDF/Excel
    private String exportFormat;        // PDF, EXCEL, CSV

    private LocalDateTime generatedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.reportStatus = ReportStatus.GENERATING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
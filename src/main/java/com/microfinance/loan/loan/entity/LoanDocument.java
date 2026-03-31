package com.microfinance.loan.loan.entity;

import com.microfinance.loan.common.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which loan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    // Uploaded by
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Users uploadedBy;

    private String uploadedByRole;      // ROLE_USER, ROLE_AGENT, ROLE_OFFICER

    // Document Info
    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String documentType;        // SANCTION_LETTER, AGREEMENT,
    // DISBURSEMENT_ADVICE, NOC,
    // REPAYMENT_SCHEDULE, OTHER

    @Column(nullable = false)
    private String fileUrl;             // S3 or storage URL

    private String fileName;
    private String mimeType;
    private String fileSize;

    // Visibility
    private Boolean visibleToUser;      // sanction letter, agreement = true
    private Boolean visibleToAgent;
    private Boolean isSystemGenerated;  // auto-generated docs

    private String remarks;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.visibleToUser = false;
        this.visibleToAgent = false;
        this.isSystemGenerated = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
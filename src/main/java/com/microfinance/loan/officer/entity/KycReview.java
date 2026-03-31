package com.microfinance.loan.officer.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.user.entity.KycDocument;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kycReviewCode;       // e.g. KYCR-00123

    // Which document is being reviewed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kyc_document_id", nullable = false)
    private KycDocument kycDocument;

    // Which officer reviewed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private Users officer;

    // Review Outcome
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus reviewStatus;

    // Verification Checks
    private Boolean nameMatches;        // name on doc matches profile
    private Boolean photoMatches;       // photo matches applicant
    private Boolean numberValid;        // aadhaar/pan number valid format
    private Boolean documentNotExpired;
    private Boolean documentNotTampered;

    // If rejected
    private String rejectionReason;
    private String rejectionCategory;   // BLURRY_IMAGE, TAMPERED,
    // NAME_MISMATCH, EXPIRED, INVALID_NUMBER

    // If resubmission requested
    private Boolean resubmissionRequested;
    private String resubmissionInstructions;

    private String officerRemarks;

    private LocalDateTime reviewedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.reviewStatus = KycStatus.PENDING;
        this.nameMatches = false;
        this.photoMatches = false;
        this.numberValid = false;
        this.documentNotExpired = false;
        this.documentNotTampered = false;
        this.resubmissionRequested = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
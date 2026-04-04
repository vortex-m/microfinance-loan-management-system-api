package com.microfinance.loan.user.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    //Doc Info
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycDocumentType documentType;

    @Column(nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    private String fileUrl;

    private String fileName;
    private String fileSize;
    private String mimeType;  // img/jpeg, pdf

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus verificationStatus;

    private String rejectedReason;
    private String officerRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Users reviewedBy;

    private LocalDateTime  reviewedAt;

    @Column(nullable = false)
    private Integer version;
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.verificationStatus = KycStatus.PENDING;
        this.version = 1;
        this.isActive = true;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

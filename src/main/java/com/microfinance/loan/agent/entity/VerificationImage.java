package com.microfinance.loan.agent.entity;

import com.microfinance.loan.common.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_report_id", nullable = false)
    private VerificationReport verificationReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Users agent;

    // Image Info
    @Column(nullable = false)
    private String fileUrl;

    private String fileName;
    private String mimeType;            // image/jpeg, image/png
    private String fileSize;

    private String imageTag;            // HOUSE_FRONT, HOUSE_INSIDE,
    // BUSINESS, APPLICANT_PHOTO, OTHER

    private String description;

    // Geo-tag at time of capture
    private Double captureLatitude;
    private Double captureLongitude;
    private LocalDateTime capturedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
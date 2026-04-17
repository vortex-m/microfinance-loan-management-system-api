package com.microfinance.loan.officer.dto.response;

import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationEvidenceResponse {

    private Long loanApplicationId;
    private Long assignedAgentId;
    private String assignedAgentName;

    private Long verificationTaskId;
    private TaskStatus verificationTaskStatus;
    private LocalDateTime verificationTaskStartedAt;
    private LocalDateTime verificationTaskCompletedAt;

    private VerificationStatus verificationStatus;
    private LocalDateTime visitedAt;
    private LocalDateTime submittedAt;
    private Double visitLatitude;
    private Double visitLongitude;
    private String visitAddress;
    private String reportSummary;
    private String riskNotes;

    private Integer imageCount;
    private List<ImageEvidenceItem> images;

    private LocalDateTime collectionPlannedAt;
    private LocalDateTime collectionStartedAt;
    private Double collectionStartedLat;
    private Double collectionStartedLng;
    private LocalDateTime collectionVerifiedAt;
    private Double collectionVerifiedLat;
    private Double collectionVerifiedLng;

    private Double agentLastLatitude;
    private Double agentLastLongitude;
    private LocalDateTime agentLastLocationUpdatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageEvidenceItem {
        private Long imageId;
        private String fileUrl;
        private String imageTag;
        private String description;
        private Double captureLatitude;
        private Double captureLongitude;
        private LocalDateTime capturedAt;
    }
}


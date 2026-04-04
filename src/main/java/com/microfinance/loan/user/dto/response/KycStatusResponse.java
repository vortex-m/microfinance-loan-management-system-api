package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycStatusResponse {

    private KycStatus overallKycStatus;     // from user_profiles

    private List<KycDocumentItem> documents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KycDocumentItem {
        private Long documentId;
        private KycDocumentType documentType;
        private String documentNumber;
        private String fileUrl;
        private String fileName;
        private String mimeType;
        private String fileSize;
        private KycStatus verificationStatus;
        private String rejectionReason;
        private String officerRemarks;
        private Integer version;
        private Boolean isActive;
        private LocalDateTime uploadedAt;
        private LocalDateTime reviewedAt;
    }
}
package com.microfinance.loan.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankProofUploadResponse {
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}

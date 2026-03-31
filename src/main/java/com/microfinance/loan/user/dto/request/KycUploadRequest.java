package com.microfinance.loan.user.dto.request;

import com.microfinance.loan.common.enums.FileType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycUploadRequest {

    @NotNull(message = "Document type is required")
    private FileType documentType;      // AADHAAR, PAN, PHOTO, SIGNATURE etc.

    @NotBlank(message = "Document number is required")
    private String documentNumber;      // actual aadhaar/pan number

    // File will come as MultipartFile in controller
    // fileUrl will be set after upload to S3
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private String fileSize;
}
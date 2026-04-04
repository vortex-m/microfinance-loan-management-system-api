package com.microfinance.loan.user.dto.request;

import com.microfinance.loan.common.enums.KycDocumentType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycUploadRequest {

    @NotNull(message = "Document type is required")
    private KycDocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;


    private String fileUrl;
    private String fileName;
    private String mimeType;
    private String fileSize;
}
package com.microfinance.loan.agent.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationSubmissionRequest {

    @NotNull(message = "Report is required")
    @Valid
    private VerificationReportRequest report;

    @NotEmpty(message = "At least one image metadata entry is required")
    @Valid
    private List<VerificationImageRequest> images;
}

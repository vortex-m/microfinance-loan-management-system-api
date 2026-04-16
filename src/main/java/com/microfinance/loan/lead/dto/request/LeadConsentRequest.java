package com.microfinance.loan.lead.dto.request;

import com.microfinance.loan.common.enums.ConsentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadConsentRequest {
    @NotNull(message = "Consent mode is required")
    private ConsentMode consentMode;

    @NotBlank(message = "Consent text is required")
    private String consentText;

    private String consentProofUrl;
    private String witnessName;
    private String witnessPhone;
}


package com.microfinance.loan.lead.dto.request;

import com.microfinance.loan.common.enums.ConsentMode;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
    private String guardianPhone;
    private String email;

    private String village;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Branch code is required")
    private String branchCode;

    private ConsentMode consentMode;
    private String consentText;
}


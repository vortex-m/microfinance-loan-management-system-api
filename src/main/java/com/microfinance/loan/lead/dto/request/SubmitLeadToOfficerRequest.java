package com.microfinance.loan.lead.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitLeadToOfficerRequest {
    private Long officerUserId;
    private String remarks;
}


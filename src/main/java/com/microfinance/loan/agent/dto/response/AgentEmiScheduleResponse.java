package com.microfinance.loan.agent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentEmiScheduleResponse {
    private Long emiScheduleId;
    private Integer emiNumber;
    private LocalDate dueDate;
    private String emiStatus;
    private Double emiAmount;
    private Double principalComponent;
    private Double interestComponent;
    private Double penaltyAmount;
    private Double partialPaidAmount;
    private Double remainingAmount;
    private Double outstandingDueAmount;
    private Double paidAmount;
    private LocalDate paidDate;
    private String paymentReference;
}


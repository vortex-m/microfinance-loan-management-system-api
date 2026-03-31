package com.microfinance.loan.user.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiScheduleResponse {

    private String loanNumber;
    private Double principalAmount;
    private Double interestRate;
    private Integer totalEmis;
    private Double emiAmount;

    private List<EmiItem> schedule;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmiItem {
        private Long emiScheduleId;
        private Integer emiNumber;
        private LocalDate dueDate;
        private Double emiAmount;
        private Double principalComponent;
        private Double interestComponent;
        private Double outstandingPrincipal;
        private String emiStatus;           // PENDING, PAID, OVERDUE
        private Double paidAmount;
        private LocalDate paidDate;
        private Double penaltyAmount;
        private Integer daysOverdue;
    }
}
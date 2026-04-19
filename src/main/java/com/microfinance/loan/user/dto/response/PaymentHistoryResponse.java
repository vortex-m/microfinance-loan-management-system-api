package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.PaymentStatus;
import com.microfinance.loan.common.enums.CashSettlementStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {

    private List<PaymentItem> payments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentItem {
        private Long paymentId;
        private String paymentNumber;
        private Long loanId;
        private String loanNumber;
        private Long emiScheduleId;
        private Integer emiNumber;
        private Double totalPaidAmount;
        private Double principalPaid;
        private Double interestPaid;
        private Double penaltyPaid;
        private String paymentMode;
        private PaymentStatus paymentStatus;
        private String gatewayTransactionId;
        private String paymentReference;
        private CashSettlementStatus cashSettlementStatus;
        private LocalDateTime cashVerifiedAt;
        private LocalDateTime settledAt;
        private LocalDateTime paidAt;
        private String receiptNumber;
    }
}
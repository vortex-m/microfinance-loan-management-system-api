package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.PaymentStatus;
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
        private String loanNumber;
        private Integer emiNumber;
        private Double totalPaidAmount;
        private Double principalPaid;
        private Double interestPaid;
        private Double penaltyPaid;
        private String paymentMode;
        private PaymentStatus paymentStatus;
        private String gatewayTransactionId;
        private LocalDateTime paidAt;
        private String receiptNumber;
    }
}
package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.CashOtpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashDisbursalOtpResponse {
    private Long loanApplicationId;
    private CashOtpStatus otpStatus;
    private Integer attempts;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private String otp;
    private String message;
}

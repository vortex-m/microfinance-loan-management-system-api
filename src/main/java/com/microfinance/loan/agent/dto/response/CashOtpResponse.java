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
public class CashOtpResponse {
    private Long otpId;
    private Long taskId;
    private CashOtpStatus otpStatus;
    private LocalDateTime expiresAt;
    private Integer attemptCount;
    private Integer maxAttempts;
}


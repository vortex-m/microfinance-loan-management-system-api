package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.VerificationStatus;
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
public class VerificationSubmissionResponse {

    private Long taskId;
    private TaskStatus taskStatus;
    private Long reportId;
    private VerificationStatus verificationStatus;
    private Integer imageCount;
    private LocalDateTime completedAt;
}

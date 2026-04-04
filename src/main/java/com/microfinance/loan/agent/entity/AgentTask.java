package com.microfinance.loan.agent.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String taskCode;

    // Which loan this task is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    // Assigned agent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Users agent;

    // Assigned by (officer or system)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private Users assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus taskStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentTaskType taskType;

    // Task Details
    private String taskDescription;
    private String priorityLevel;

    // Deadline
    private LocalDateTime deadline;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private Boolean otpRequired;
    private Boolean otpVerified;
    private LocalDateTime otpRequestedAt;
    private LocalDateTime otpVerifiedAt;

    // Decline Info
    private String declineReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.taskStatus = TaskStatus.ASSIGNED;
        if (this.taskType == null) {
            this.taskType = AgentTaskType.VERIFICATION;
        }
        if (this.otpRequired == null) {
            this.otpRequired = false;
        }
        if (this.otpVerified == null) {
            this.otpVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
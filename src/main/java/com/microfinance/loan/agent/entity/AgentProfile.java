package com.microfinance.loan.agent.entity;


import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.common.enums.AgentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;

    @Column(nullable = false, unique = true)
    private String agentCode;

    private String designation;
    private String department;

    private String assignedCity;
    private String assignedState;
    private String assignedZone;

    private LocalDateTime joinedDate;
    private String employeeId;
    private String supervisorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus agentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentAvailability  agentAvailability;

    private Integer totalVerifications;
    private Integer completedVerifications;
    private Integer pendingVerifications;
    private Double performanceScore;

    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDateTime lastLocationUpdateAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.agentStatus == null) {
            this.agentStatus = AgentStatus.UNDER_REVIEW;
        }
        if(this.agentAvailability == null) {
            this.agentAvailability = AgentAvailability.AVAILABLE;
        }
        this.totalVerifications = 0;
        this.completedVerifications = 0;
        this.pendingVerifications = 0;
        this.performanceScore = 0.0;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}

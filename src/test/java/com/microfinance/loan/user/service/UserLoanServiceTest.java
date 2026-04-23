package com.microfinance.loan.user.service;

import com.microfinance.loan.agent.dto.request.AgentLoanApplyForUserRequest;
import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.UserStatus;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLoanServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private KycDocumentRepository kycDocumentRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanEmiScheduleRepository loanEmiScheduleRepository;
    @Mock
    private AgentProfileRepository agentProfileRepository;
    @Mock
    private AgentTaskRepository agentTaskRepository;

    @InjectMocks
    private UserLoanService userLoanService;

    @Test
    void applyForLoanByAgent_shouldCreateVerificationTaskForAssignedAgent() {
        Long userId = 501L;
        Long agentUserId = 701L;

        Users borrower = Users.builder()
                .id(userId)
                .name("Borrower")
                .status(UserStatus.ACTIVE)
                .isHome(true)
                .build();

        Users agentUser = Users.builder()
                .id(agentUserId)
                .name("Agent One")
                .build();

        BranchProfile branch = BranchProfile.builder()
                .id(11L)
                .branchCode("BR001")
                .branchName("Main Branch")
                .regionName("North Zone")
                .build();

        AgentProfile agentProfile = AgentProfile.builder()
                .id(41L)
                .users(agentUser)
                .branchProfile(branch)
                .build();

        UserProfile userProfile = UserProfile.builder()
                .id(91L)
                .users(borrower)
                .branchProfile(branch)
                .kycStatus(KycStatus.VERIFIED)
                .creditScore(720d)
                .riskScore(0d)
                .scoreUpdatedAt(LocalDateTime.now())
                .build();

        AgentLoanApplyForUserRequest request = AgentLoanApplyForUserRequest.builder()
                .userId(userId)
                .requestedAmount(10000d)
                .tenureMonths(12)
                .loanPurpose("Business")
                .disbursalMode(DisbursalMode.CASH)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(borrower));
        when(agentProfileRepository.findByUsersIdWithBranch(agentUserId)).thenReturn(Optional.of(agentProfile));
        when(userProfileRepository.findByUsersId(userId)).thenReturn(Optional.of(userProfile));
        when(kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(userId, KycDocumentType.AADHAAR, KycStatus.VERIFIED))
                .thenReturn(true);
        when(kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(userId, KycDocumentType.PAN, KycStatus.VERIFIED))
                .thenReturn(true);

        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(invocation -> {
            LoanApplication app = invocation.getArgument(0);
            if (app.getId() == null) {
                app.setId(1001L);
            }
            return app;
        });

        when(agentTaskRepository.findTopByLoanApplicationIdAndTaskTypeAndTaskStatusInOrderByCreatedAtDesc(
                1001L,
                AgentTaskType.VERIFICATION,
                List.of(TaskStatus.ASSIGNED, TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS)
        )).thenReturn(Optional.empty());
        when(agentTaskRepository.save(any(AgentTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplyResponse response = userLoanService.applyForLoanByAgent(agentUserId, request);

        assertNotNull(response);
        assertEquals(agentUserId, response.getAssignedAgentId());

        ArgumentCaptor<AgentTask> taskCaptor = ArgumentCaptor.forClass(AgentTask.class);
        verify(agentTaskRepository).save(taskCaptor.capture());
        assertEquals(AgentTaskType.VERIFICATION, taskCaptor.getValue().getTaskType());
        assertEquals(TaskStatus.ASSIGNED, taskCaptor.getValue().getTaskStatus());
        assertEquals(1001L, taskCaptor.getValue().getLoanApplication().getId());
        assertEquals(agentUserId, taskCaptor.getValue().getAgent().getId());
    }
}





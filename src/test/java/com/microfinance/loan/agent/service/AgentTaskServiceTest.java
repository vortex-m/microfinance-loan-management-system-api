package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.response.AgentTaskResponse;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.CashCollectionOtpRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.loan.service.LoanService;
import com.microfinance.loan.payment.repository.TransactionRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentTaskServiceTest {

    @Mock
    private AgentTaskRepository agentTaskRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanEmiScheduleRepository loanEmiScheduleRepository;
    @Mock
    private CashCollectionOtpRepository cashCollectionOtpRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private LoanService loanService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailService mailService;

    @InjectMocks
    private AgentTaskService agentTaskService;

    @Test
    void acceptTask_shouldMoveAssignedTaskToAccepted() {
        Users agent = Users.builder().id(101L).build();
        AgentTask task = AgentTask.builder().id(11L).taskStatus(TaskStatus.ASSIGNED).agent(agent).build();
        when(agentTaskRepository.findById(11L)).thenReturn(Optional.of(task));
        when(agentTaskRepository.save(any(AgentTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgentTaskResponse response = agentTaskService.acceptTask(101L, 11L);

        assertEquals(TaskStatus.ACCEPTED, response.getTaskStatus());
        assertNotNull(response.getAcceptedAt());
    }

    @Test
    void declineTask_shouldRequireReason() {
        Users agent = Users.builder().id(101L).build();
        AgentTask task = AgentTask.builder().id(12L).taskStatus(TaskStatus.ASSIGNED).agent(agent).build();
        when(agentTaskRepository.findById(12L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class,
                () -> agentTaskService.declineTask(101L, 12L, "  "));
    }

    @Test
    void acceptTask_shouldRejectTaskAssignedToAnotherAgent() {
        Users assignedAgent = Users.builder().id(999L).build();
        AgentTask assignedTask = AgentTask.builder()
                .id(13L)
                .taskStatus(TaskStatus.ASSIGNED)
                .agent(assignedAgent)
                .build();
        when(agentTaskRepository.findById(13L)).thenReturn(Optional.of(assignedTask));

        assertThrows(IllegalArgumentException.class,
                () -> agentTaskService.acceptTask(101L, 13L));
    }
}


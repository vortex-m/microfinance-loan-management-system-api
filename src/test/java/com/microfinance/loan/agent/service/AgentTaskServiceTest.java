package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.response.AgentTaskResponse;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.CashCollectionOtpRepository;
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

import java.time.LocalDateTime;
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
        AgentTask task = AgentTask.builder().id(11L).taskStatus(TaskStatus.ASSIGNED).build();
        when(agentTaskRepository.findByIdAndAgentId(11L, 101L)).thenReturn(Optional.of(task));
        when(agentTaskRepository.save(any(AgentTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgentTaskResponse response = agentTaskService.acceptTask(101L, 11L);

        assertEquals(TaskStatus.ACCEPTED, response.getTaskStatus());
        assertNotNull(response.getAcceptedAt());
    }

    @Test
    void declineTask_shouldRequireReason() {
        AgentTask task = AgentTask.builder().id(12L).taskStatus(TaskStatus.ASSIGNED).build();
        when(agentTaskRepository.findByIdAndAgentId(12L, 101L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class,
                () -> agentTaskService.declineTask(101L, 12L, "  "));
    }
}


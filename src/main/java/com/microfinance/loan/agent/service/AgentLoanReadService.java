package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.response.AgentAssignedLoanResponse;
import com.microfinance.loan.agent.dto.response.AgentEmiScheduleResponse;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AgentLoanReadService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final LoanEmiScheduleRepository loanEmiScheduleRepository;

    public AgentLoanReadService(LoanApplicationRepository loanApplicationRepository,
                                LoanRepository loanRepository,
                                LoanEmiScheduleRepository loanEmiScheduleRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepository = loanRepository;
        this.loanEmiScheduleRepository = loanEmiScheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<AgentAssignedLoanResponse> listAssignedApplications(Long agentUserId) {
        return loanApplicationRepository.findByAssignedAgentIdOrderByUpdatedAtDesc(agentUserId)
                .stream()
                .map(application -> {
                    Loan loan = loanRepository.findByLoanApplicationId(application.getId()).orElse(null);
                    LocalDate nextDueDate = loan == null
                            ? null
                            : loanEmiScheduleRepository
                            .findFirstByLoanIdAndEmiStatusInOrderByEmiNumberAsc(loan.getId(), List.of("PENDING", "OVERDUE", "PARTIALLY_PAID"))
                            .map(LoanEmiSchedule::getDueDate)
                            .orElse(null);
                    return toAssignedLoanResponse(application, loan, nextDueDate);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgentEmiScheduleResponse> listLoanEmis(Long agentUserId, Long loanId) {
        Loan loan = loanRepository.findByIdAndVerifiedByAgentId(loanId, agentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found for this agent: " + loanId));

        return loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId())
                .stream()
                .map(this::toEmiResponse)
                .toList();
    }

    private AgentAssignedLoanResponse toAssignedLoanResponse(LoanApplication application, Loan loan, LocalDate nextDueDate) {
        return AgentAssignedLoanResponse.builder()
                .loanApplicationId(application.getId())
                .applicationNumber(application.getApplicationNumber())
                .applicationStatus(application.getStatus())
                .requestedAmount(application.getRequestedAmount())
                .approvedAmount(application.getApprovedAmount())
                .tenureMonths(application.getTenureMonths())
                .loanPurpose(application.getLoanPurpose())
                .disbursalMode(application.getDisbursalMode())
                .userId(application.getUser() != null ? application.getUser().getId() : null)
                .userName(application.getUser() != null ? application.getUser().getName() : null)
                .userPhone(application.getUser() != null ? application.getUser().getPhone() : null)
                .userEmail(application.getUser() != null ? application.getUser().getEmail() : null)
                .loanId(loan != null ? loan.getId() : null)
                .loanNumber(loan != null ? loan.getLoanNumber() : null)
                .loanStatus(loan != null ? loan.getLoanStatus() : null)
                .emiAmount(loan != null ? loan.getEmiAmount() : null)
                .totalEmis(loan != null ? loan.getTotalEmis() : null)
                .emisPending(loan != null ? loan.getEmisPending() : null)
                .emisOverdue(loan != null ? loan.getEmisOverdue() : null)
                .totalPaidAmount(loan != null ? loan.getTotalPaidAmount() : null)
                .outstandingPrincipal(loan != null ? loan.getOutstandingPrincipal() : null)
                .nextDueDate(nextDueDate)
                .appliedAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private AgentEmiScheduleResponse toEmiResponse(LoanEmiSchedule emi) {
        double fullDue = safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount());
        double outstandingDue = round(Math.max(0d, fullDue - safe(emi.getPartialPaidAmount())));

        return AgentEmiScheduleResponse.builder()
                .emiScheduleId(emi.getId())
                .emiNumber(emi.getEmiNumber())
                .dueDate(emi.getDueDate())
                .emiStatus(emi.getEmiStatus())
                .emiAmount(emi.getEmiAmount())
                .principalComponent(emi.getPrincipalComponent())
                .interestComponent(emi.getInterestComponent())
                .penaltyAmount(emi.getPenaltyAmount())
                .partialPaidAmount(emi.getPartialPaidAmount())
                .remainingAmount(emi.getRemainingAmount())
                .outstandingDueAmount(outstandingDue)
                .paidAmount(emi.getPaidAmount())
                .paidDate(emi.getPaidDate())
                .paymentReference(emi.getPaymentReference())
                .build();
    }

    private double safe(Double value) {
        return value == null ? 0d : value;
    }

    private double round(double value) {
        return Math.round(value * 100d) / 100d;
    }
}


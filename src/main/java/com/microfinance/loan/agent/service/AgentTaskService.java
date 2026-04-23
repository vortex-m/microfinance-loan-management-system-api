package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.CashDisbursalOtpGenerateRequest;
import com.microfinance.loan.agent.dto.request.CashDisbursalOtpVerifyRequest;
import com.microfinance.loan.agent.dto.request.CashOtpRequest;
import com.microfinance.loan.agent.dto.request.CashOtpVerifyRequest;
import com.microfinance.loan.agent.dto.response.AgentTaskResponse;
import com.microfinance.loan.agent.dto.response.CashDisbursalOtpResponse;
import com.microfinance.loan.agent.dto.response.CashOtpResponse;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.entity.CashCollectionOtp;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.CashCollectionOtpRepository;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.CashOtpStatus;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.PaymentStatus;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.loan.service.LoanService;
import com.microfinance.loan.payment.entity.Payment;
import com.microfinance.loan.payment.repository.TransactionRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AgentTaskService {

    private static final int OTP_MAX_ATTEMPTS = 3;
    private static final double EPSILON = 0.0001d;

    private final AgentTaskRepository agentTaskRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final LoanEmiScheduleRepository loanEmiScheduleRepository;
    private final CashCollectionOtpRepository cashCollectionOtpRepository;
    private final TransactionRepository transactionRepository;
    private final LoanService loanService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AgentTaskService(AgentTaskRepository agentTaskRepository,
                            LoanApplicationRepository loanApplicationRepository,
                            LoanRepository loanRepository,
                            LoanEmiScheduleRepository loanEmiScheduleRepository,
                            CashCollectionOtpRepository cashCollectionOtpRepository,
                            TransactionRepository transactionRepository,
                            LoanService loanService,
                            PasswordEncoder passwordEncoder,
                            MailService mailService) {
        this.agentTaskRepository = agentTaskRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepository = loanRepository;
        this.loanEmiScheduleRepository = loanEmiScheduleRepository;
        this.cashCollectionOtpRepository = cashCollectionOtpRepository;
        this.transactionRepository = transactionRepository;
        this.loanService = loanService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<AgentTaskResponse> listMyTasks(Long agentId, TaskStatus taskStatus, AgentTaskType taskType) {
        List<AgentTask> tasks;
        if (taskStatus != null && taskType != null) {
            tasks = agentTaskRepository.findByAgentIdAndTaskStatusAndTaskTypeOrderByCreatedAtDesc(agentId, taskStatus, taskType);
        } else if (taskStatus != null) {
            tasks = agentTaskRepository.findByAgentIdAndTaskStatusOrderByCreatedAtDesc(agentId, taskStatus);
        } else if (taskType != null) {
            tasks = agentTaskRepository.findByAgentIdAndTaskTypeOrderByCreatedAtDesc(agentId, taskType);
        } else {
            tasks = agentTaskRepository.findByAgentIdOrderByCreatedAtDesc(agentId);
        }

        return tasks.stream().map(this::toTaskResponse).toList();
    }

    @Transactional(readOnly = true)
    public AgentTaskResponse getTaskDetail(Long agentId, Long taskId) {
        return toTaskResponse(getAgentTask(taskId, agentId));
    }

    @Transactional
    public AgentTaskResponse acceptTask(Long agentId, Long taskId) {
        AgentTask task = getAgentTask(taskId, agentId);
        if (task.getTaskStatus() != TaskStatus.ASSIGNED) {
            throw new IllegalArgumentException("Only ASSIGNED tasks can be accepted");
        }
        task.setTaskStatus(TaskStatus.ACCEPTED);
        task.setAcceptedAt(LocalDateTime.now());
        return toTaskResponse(agentTaskRepository.save(task));
    }

    @Transactional
    public AgentTaskResponse startTask(Long agentId, Long taskId) {
        AgentTask task = getAgentTask(taskId, agentId);
        if (task.getTaskStatus() != TaskStatus.ACCEPTED && task.getTaskStatus() != TaskStatus.ASSIGNED) {
            throw new IllegalArgumentException("Only ASSIGNED or ACCEPTED tasks can be started");
        }
        if (task.getAcceptedAt() == null) {
            task.setAcceptedAt(LocalDateTime.now());
        }
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        return toTaskResponse(agentTaskRepository.save(task));
    }

    @Transactional
    public AgentTaskResponse completeTask(Long agentId, Long taskId) {
        AgentTask task = getAgentTask(taskId, agentId);
        if (task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only IN_PROGRESS tasks can be completed");
        }
        task.setTaskStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        return toTaskResponse(agentTaskRepository.save(task));
    }

    @Transactional
    public AgentTaskResponse declineTask(Long agentId, Long taskId, String reason) {
        AgentTask task = getAgentTask(taskId, agentId);
        if (task.getTaskStatus() != TaskStatus.ASSIGNED && task.getTaskStatus() != TaskStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only ASSIGNED or ACCEPTED tasks can be declined");
        }
        if (task.getAgent() == null || !task.getAgent().getId().equals(agentId)) {
            throw new IllegalArgumentException("Claim the task before declining it");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Decline reason is required");
        }
        task.setTaskStatus(TaskStatus.DECLINED);
        task.setDeclineReason(reason.trim());
        return toTaskResponse(agentTaskRepository.save(task));
    }

    @Transactional
    public CashDisbursalOtpResponse generateCashDisbursalOtp(Long agentId, CashDisbursalOtpGenerateRequest request) {
        AgentTask task = getAgentTask(request.getTaskId(), agentId);
        validateCashDisbursalTask(task);
        LoanApplication application = task.getLoanApplication();

        validateCashOtpEligibility(application);
        if (!Boolean.TRUE.equals(task.getOtpVerified())) {
            throw new IllegalArgumentException("Officer handover OTP is not verified yet for this task");
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        application.setCashDisbursalOtpHash(passwordEncoder.encode(otp));
        application.setCashDisbursalOtpStatus(CashOtpStatus.ACTIVE);
        application.setCashDisbursalOtpAttempts(0);
        application.setCashDisbursalOtpRequestedAt(LocalDateTime.now());
        application.setCashDisbursalOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        application.setCashDisbursalOtpVerifiedAt(null);

        loanApplicationRepository.save(application);

        if (application.getUser().getEmail() != null) {
            mailService.sendCashDisbursalOtp(
                    application.getUser().getEmail(),
                    application.getUser().getName(),
                    otp,
                    application.getApplicationNumber()
            );
        }

        return CashDisbursalOtpResponse.builder()
                .loanApplicationId(application.getId())
                .otpStatus(application.getCashDisbursalOtpStatus())
                .attempts(application.getCashDisbursalOtpAttempts())
                .expiresAt(application.getCashDisbursalOtpExpiresAt())
                .otp(null)
                .message("Cash receipt OTP generated and sent to borrower email")
                .build();
    }

    @Transactional
    public CashDisbursalOtpResponse verifyCashDisbursalOtp(Long agentId, CashDisbursalOtpVerifyRequest request) {
        AgentTask task = getAgentTask(request.getTaskId(), agentId);
        validateCashDisbursalTask(task);
        LoanApplication application = task.getLoanApplication();

        if (application.getCashDisbursalOtpStatus() != CashOtpStatus.ACTIVE || application.getCashDisbursalOtpHash() == null) {
            throw new IllegalArgumentException("No active OTP found for this loan application.");
        }

        if (application.getCashDisbursalOtpExpiresAt() == null || LocalDateTime.now().isAfter(application.getCashDisbursalOtpExpiresAt())) {
            application.setCashDisbursalOtpStatus(CashOtpStatus.EXPIRED);
            loanApplicationRepository.save(application);
            throw new IllegalArgumentException("OTP expired. Please generate a new OTP.");
        }

        if (passwordEncoder.matches(request.getOtp(), application.getCashDisbursalOtpHash())) {
            application.setCashDisbursalOtpStatus(CashOtpStatus.USED);
            application.setCashDisbursalOtpVerifiedAt(LocalDateTime.now());
            application.setDisbursedAt(LocalDateTime.now());
            application.setDisbursalReference("CASH-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            application.setStatus(LoanStatus.DISBURSED);
            loanApplicationRepository.save(application);

            Loan bookedLoan = loanRepository.findByLoanApplicationId(application.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Booked loan not found for application: " + application.getId()));
            loanService.markCashDisbursed(bookedLoan, task.getAgent());
            ensureCashCollectionTask(application, task.getAgent());

            task.setTaskStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            agentTaskRepository.save(task);

            return CashDisbursalOtpResponse.builder()
                    .loanApplicationId(application.getId())
                    .otpStatus(application.getCashDisbursalOtpStatus())
                    .attempts(application.getCashDisbursalOtpAttempts())
                    .expiresAt(application.getCashDisbursalOtpExpiresAt())
                    .verifiedAt(application.getCashDisbursalOtpVerifiedAt())
                    .message("OTP verified and cash disbursal marked successful.")
                    .build();
        }

        int attempts = application.getCashDisbursalOtpAttempts() == null ? 0 : application.getCashDisbursalOtpAttempts();
        attempts++;
        application.setCashDisbursalOtpAttempts(attempts);
        if (attempts >= OTP_MAX_ATTEMPTS) {
            application.setCashDisbursalOtpStatus(CashOtpStatus.BLOCKED);
        }
        loanApplicationRepository.save(application);

        throw new IllegalArgumentException(attempts >= OTP_MAX_ATTEMPTS
                ? "OTP blocked due to maximum invalid attempts. Generate a new OTP."
                : "Invalid OTP. Please try again.");
    }

    private void validateCashOtpEligibility(LoanApplication application) {
        if (application.getDisbursalMode() != DisbursalMode.CASH) {
            throw new IllegalArgumentException("OTP cash disbursal is allowed only for CASH mode loans.");
        }
        if (application.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Loan must be APPROVED before cash OTP generation.");
        }
    }

    private void validateCashDisbursalTask(AgentTask task) {
        if (task.getTaskType() != AgentTaskType.CASH_DISBURSAL) {
            throw new IllegalArgumentException("Task is not of CASH_DISBURSAL type");
        }
        if (task.getLoanApplication() == null) {
            throw new IllegalArgumentException("Task is not linked to a loan application");
        }
        if (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.DECLINED) {
            throw new IllegalArgumentException("Task is not active: " + task.getTaskStatus());
        }
    }

    @Transactional
    public CashOtpResponse generateCollectionOtp(Long agentId, CashOtpRequest request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + request.getLoanId()));

        AgentTask task = getAgentTask(request.getTaskId(), agentId);
        validateCashCollectionTask(task, loan);

        if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
            throw new IllegalArgumentException("Collection is allowed only after loan is DISBURSED");
        }

        LoanEmiSchedule emi = loanEmiScheduleRepository.findById(request.getEmiScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("EMI schedule not found: " + request.getEmiScheduleId()));
        if (!emi.getLoan().getId().equals(loan.getId())) {
            throw new IllegalArgumentException("EMI does not belong to this loan");
        }
        if ("PAID".equalsIgnoreCase(emi.getEmiStatus())) {
            throw new IllegalArgumentException("EMI is already paid");
        }

        double dueAmount = getOutstandingDueAmount(emi);
        if (request.getCollectionAmount() > dueAmount + EPSILON) {
            throw new IllegalArgumentException("Collection amount cannot exceed outstanding EMI due amount");
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        CashCollectionOtp otpRecord = CashCollectionOtp.builder()
                .loan(loan)
                .emiSchedule(emi)
                .user(loan.getUser())
                .agent(task.getAgent())
                .task(task)
                .otpHash(passwordEncoder.encode(otp))
                .requestedCollectionAmount(round(request.getCollectionAmount()))
                .otpStatus(CashOtpStatus.ACTIVE)
                .attemptCount(0)
                .maxAttempts(OTP_MAX_ATTEMPTS)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        CashCollectionOtp savedOtp = cashCollectionOtpRepository.save(otpRecord);

        task.setOtpRequired(true);
        task.setOtpRequestedAt(LocalDateTime.now());
        task.setCollectionPlannedAt(request.getCollectionPlannedAt());
        task.setCollectionStartedAt(LocalDateTime.now());
        task.setCollectionStartedLat(request.getAgentLatitude());
        task.setCollectionStartedLng(request.getAgentLongitude());
        if (task.getTaskStatus() == TaskStatus.ASSIGNED || task.getTaskStatus() == TaskStatus.ACCEPTED) {
            task.setTaskStatus(TaskStatus.IN_PROGRESS);
            if (task.getStartedAt() == null) {
                task.setStartedAt(LocalDateTime.now());
            }
        }
        agentTaskRepository.save(task);

        if (loan.getUser().getEmail() != null) {
            mailService.sendCashDisbursalOtp(
                    loan.getUser().getEmail(),
                    loan.getUser().getName(),
                    otp,
                    loan.getLoanNumber()
            );
        }

        return CashOtpResponse.builder()
                .otpId(savedOtp.getId())
                .taskId(request.getTaskId())
                .otpStatus(savedOtp.getOtpStatus())
                .expiresAt(savedOtp.getExpiresAt())
                .attemptCount(savedOtp.getAttemptCount())
                .maxAttempts(savedOtp.getMaxAttempts())
                .build();
    }

    @Transactional
    public CashOtpResponse verifyCollectionOtp(Long agentId, CashOtpVerifyRequest request) {
        CashCollectionOtp otpRecord = cashCollectionOtpRepository
                .findByIdAndAgentIdAndTaskId(request.getOtpId(), agentId, request.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Collection OTP record not found for this agent"));

        AgentTask task = getAgentTask(request.getTaskId(), agentId);
        validateCashCollectionTask(task, otpRecord.getLoan());

        if (otpRecord.getTask() == null || !otpRecord.getTask().getId().equals(task.getId())) {
            throw new IllegalArgumentException("OTP does not belong to the provided task");
        }

        if (otpRecord.getOtpStatus() != CashOtpStatus.ACTIVE) {
            throw new IllegalArgumentException("OTP is not active");
        }
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            otpRecord.setOtpStatus(CashOtpStatus.EXPIRED);
            cashCollectionOtpRepository.save(otpRecord);
            throw new IllegalArgumentException("OTP expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpRecord.getOtpHash())) {
            int attempts = otpRecord.getAttemptCount() == null ? 0 : otpRecord.getAttemptCount();
            attempts++;
            otpRecord.setAttemptCount(attempts);
            if (attempts >= otpRecord.getMaxAttempts()) {
                otpRecord.setOtpStatus(CashOtpStatus.BLOCKED);
            }
            cashCollectionOtpRepository.save(otpRecord);
            throw new IllegalArgumentException(attempts >= otpRecord.getMaxAttempts()
                    ? "OTP blocked due to too many invalid attempts"
                    : "Invalid OTP");
        }

        otpRecord.setOtpStatus(CashOtpStatus.USED);
        otpRecord.setVerifiedAt(LocalDateTime.now());
        cashCollectionOtpRepository.save(otpRecord);

        LoanEmiSchedule emi = otpRecord.getEmiSchedule();
        Loan loan = otpRecord.getLoan();

        double requestedAmount = round(request.getCollectionAmount());
        if (Math.abs(requestedAmount - safe(otpRecord.getRequestedCollectionAmount())) > EPSILON) {
            throw new IllegalArgumentException("Collection amount mismatch with OTP request");
        }

        double dueAmount = getOutstandingDueAmount(emi);
        if (requestedAmount > dueAmount + EPSILON) {
            throw new IllegalArgumentException("Collection amount cannot exceed outstanding EMI due amount");
        }

        String paymentReference = "CASH-EMI-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        double ratio = dueAmount <= EPSILON ? 0d : (requestedAmount / dueAmount);
        double principalPaid = round(safe(emi.getPrincipalComponent()) * ratio);
        double interestPaid = round(safe(emi.getInterestComponent()) * ratio);
        double penaltyPaid = round(safe(emi.getPenaltyAmount()) * ratio);

        Payment payment = Payment.builder()
                .paymentNumber("PAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .loan(loan)
                .emiSchedule(emi)
                .user(loan.getUser())
                .totalPaidAmount(requestedAmount)
                .principalPaid(principalPaid)
                .interestPaid(interestPaid)
                .penaltyPaid(penaltyPaid)
                .paymentMode("CASH")
                .paymentReference(paymentReference)
                .paymentStatus(PaymentStatus.SUCCESS)
                .gatewayStatus("SUCCESS")
                .verifiedBy(otpRecord.getAgent())
                .cashCollectionOtp(otpRecord)
                .verifiedAt(LocalDateTime.now())
                .successAt(LocalDateTime.now())
                .build();
        transactionRepository.save(payment);

        double updatedPartialPaid = round(safe(emi.getPartialPaidAmount()) + requestedAmount);
        if (updatedPartialPaid + EPSILON >= (safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount()))) {
            emi.setEmiStatus("PAID");
            emi.setPaidAmount(round(safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount())));
            emi.setPaidDate(LocalDateTime.now().toLocalDate());
            emi.setRemainingAmount(0d);
        } else {
            emi.setEmiStatus("PARTIALLY_PAID");
            emi.setPaidAmount(0d);
            emi.setRemainingAmount(round((safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount())) - updatedPartialPaid));
        }
        emi.setPartialPaidAmount(updatedPartialPaid);
        emi.setPaymentReference(paymentReference);
        loanEmiScheduleRepository.save(emi);

        syncLoanAfterCollection(loan);

        task.setOtpVerified(true);
        task.setOtpVerifiedAt(LocalDateTime.now());
        task.setCollectionVerifiedAt(LocalDateTime.now());
        task.setCollectionVerifiedLat(request.getAgentLatitude());
        task.setCollectionVerifiedLng(request.getAgentLongitude());
        task.setTaskStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        agentTaskRepository.save(task);

        ensureCashCollectionTask(task.getLoanApplication(), loan, resolveNextCashTaskAssignee(task, agentId));

        return CashOtpResponse.builder()
                .otpId(otpRecord.getId())
                .taskId(task.getId())
                .otpStatus(otpRecord.getOtpStatus())
                .expiresAt(otpRecord.getExpiresAt())
                .attemptCount(otpRecord.getAttemptCount())
                .maxAttempts(otpRecord.getMaxAttempts())
                .build();
    }

    private void syncLoanAfterCollection(Loan loan) {
        List<LoanEmiSchedule> schedule = loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId());
        int paid = (int) schedule.stream().filter(emi -> "PAID".equalsIgnoreCase(emi.getEmiStatus())).count();
        int overdue = (int) schedule.stream().filter(emi -> "OVERDUE".equalsIgnoreCase(emi.getEmiStatus())).count();
        double totalPaid = schedule.stream().mapToDouble(emi -> safe(emi.getPaidAmount())).sum();
        double outstanding = schedule.stream()
                .filter(emi -> !"PAID".equalsIgnoreCase(emi.getEmiStatus()))
                .mapToDouble(emi -> safe(emi.getPrincipalComponent()))
                .sum();

        loan.setEmisPaid(paid);
        loan.setEmisPending(Math.max(0, loan.getTotalEmis() - paid));
        loan.setEmisOverdue(overdue);
        loan.setTotalPaidAmount(Math.round(totalPaid * 100d) / 100d);
        loan.setOutstandingPrincipal(Math.round(outstanding * 100d) / 100d);

        if (paid == loan.getTotalEmis()) {
            loan.setLoanStatus(LoanStatus.CLOSED);
            loan.setActualClosureDate(java.time.LocalDate.now());
            loan.setClosedAt(LocalDateTime.now());
        }

        loanRepository.save(loan);
    }

    private double safe(Double value) {
        return value == null ? 0d : value;
    }

    private double round(double value) {
        return Math.round(value * 100d) / 100d;
    }

    private double getOutstandingDueAmount(LoanEmiSchedule emi) {
        double fullDue = safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount());
        return round(Math.max(0d, fullDue - safe(emi.getPartialPaidAmount())));
    }

    private AgentTask getAgentTask(Long taskId, Long agentId) {
        AgentTask task = agentTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        if (task.getAgent() == null) {
            throw new IllegalArgumentException("Task is not assigned to any agent: " + taskId);
        }
        if (!task.getAgent().getId().equals(agentId)) {
            throw new IllegalArgumentException("Task not found for this agent: " + taskId);
        }
        return task;
    }

    private void validateCashCollectionTask(AgentTask task, Loan loan) {
        if (task.getTaskType() != AgentTaskType.CASH_COLLECTION) {
            throw new IllegalArgumentException("Task is not of CASH_COLLECTION type");
        }
        if (task.getLoanApplication() == null || loan.getLoanApplication() == null
                || !task.getLoanApplication().getId().equals(loan.getLoanApplication().getId())) {
            throw new IllegalArgumentException("Task does not belong to this loan application");
        }
        if (task.getTaskStatus() == TaskStatus.DECLINED || task.getTaskStatus() == TaskStatus.COMPLETED) {
            throw new IllegalArgumentException("Task is not active: " + task.getTaskStatus());
        }
    }

    private AgentTaskResponse toTaskResponse(AgentTask task) {
        LoanApplication application = task.getLoanApplication();
        Users borrower = application != null ? application.getUser() : null;

        Loan loan = application == null || application.getId() == null
                ? null
                : loanRepository.findByLoanApplicationId(application.getId()).orElse(null);

        LoanEmiSchedule nextEmi = loan == null
                ? null
                : loanEmiScheduleRepository
                .findFirstByLoanIdAndEmiStatusInOrderByEmiNumberAsc(loan.getId(), List.of("PENDING", "OVERDUE", "PARTIALLY_PAID"))
                .orElse(null);

        double nextEmiOutstanding = nextEmi == null
                ? 0d
                : round(Math.max(0d,
                safe(nextEmi.getEmiAmount())
                        + safe(nextEmi.getPenaltyAmount())
                        - safe(nextEmi.getPartialPaidAmount())));

        return AgentTaskResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .loanApplicationId(task.getLoanApplication() != null ? task.getLoanApplication().getId() : null)
                .applicationNumber(task.getLoanApplication() != null ? task.getLoanApplication().getApplicationNumber() : null)
                .userId(borrower != null ? borrower.getId() : null)
                .userName(borrower != null ? borrower.getName() : null)
                .userPhone(borrower != null ? borrower.getPhone() : null)
                .loanId(loan != null ? loan.getId() : null)
                .loanNumber(loan != null ? loan.getLoanNumber() : null)
                .loanStatus(loan != null && loan.getLoanStatus() != null ? loan.getLoanStatus().name() : null)
                .loanEmiAmount(loan != null ? loan.getEmiAmount() : null)
                .nextEmiDueDate(nextEmi != null ? nextEmi.getDueDate() : null)
                .nextEmiScheduleId(nextEmi != null ? nextEmi.getId() : null)
                .nextEmiOutstandingAmount(nextEmi != null ? nextEmiOutstanding : null)
                .taskType(task.getTaskType())
                .taskStatus(task.getTaskStatus())
                .taskDescription(task.getTaskDescription())
                .priorityLevel(task.getPriorityLevel())
                .otpRequired(task.getOtpRequired())
                .otpVerified(task.getOtpVerified())
                .otpRequestedAt(task.getOtpRequestedAt())
                .otpVerifiedAt(task.getOtpVerifiedAt())
                .deadline(task.getDeadline())
                .acceptedAt(task.getAcceptedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .collectionPlannedAt(task.getCollectionPlannedAt())
                .collectionStartedAt(task.getCollectionStartedAt())
                .collectionStartedLat(task.getCollectionStartedLat())
                .collectionStartedLng(task.getCollectionStartedLng())
                .collectionVerifiedAt(task.getCollectionVerifiedAt())
                .collectionVerifiedLat(task.getCollectionVerifiedLat())
                .collectionVerifiedLng(task.getCollectionVerifiedLng())
                .build();
    }

    private Users resolveNextCashTaskAssignee(AgentTask currentTask, Long fallbackAgentId) {
        if (currentTask.getAgent() != null && currentTask.getAgent().getId() != null) {
            return currentTask.getAgent();
        }
        LoanApplication application = currentTask.getLoanApplication();
        if (application != null && application.getAssignedAgent() != null && application.getAssignedAgent().getId() != null) {
            return application.getAssignedAgent();
        }
        if (fallbackAgentId == null) {
            return null;
        }
        Users fallback = new Users();
        fallback.setId(fallbackAgentId);
        return fallback;
    }

    private void ensureCashCollectionTask(LoanApplication application, com.microfinance.loan.common.entity.Users assignedAgent) {
        ensureCashCollectionTask(application, null, assignedAgent);
    }

    private void ensureCashCollectionTask(LoanApplication application,
                                          Loan knownLoan,
                                          com.microfinance.loan.common.entity.Users assignedAgent) {
        if (application == null || application.getId() == null) {
            return;
        }

        boolean alreadyOpen = agentTaskRepository.existsByLoanApplicationIdAndTaskTypeAndTaskStatusIn(
                application.getId(),
                AgentTaskType.CASH_COLLECTION,
                Arrays.asList(TaskStatus.ASSIGNED, TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS)
        );
        if (alreadyOpen) {
            return;
        }

        Loan loan = knownLoan != null
                ? knownLoan
                : loanRepository.findByLoanApplicationId(application.getId()).orElse(null);
        if (loan == null) {
            return;
        }

        LoanEmiSchedule nextEmi = loanEmiScheduleRepository
                .findFirstByLoanIdAndEmiStatusInOrderByEmiNumberAsc(loan.getId(), List.of("PENDING", "OVERDUE", "PARTIALLY_PAID"))
                .orElseGet(() -> loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId()).stream()
                        .filter(emi -> emi.getEmiStatus() == null
                                || (!"PAID".equalsIgnoreCase(emi.getEmiStatus())
                                && !"WAIVED".equalsIgnoreCase(emi.getEmiStatus())))
                        .findFirst()
                        .orElse(null));
        if (nextEmi == null) {
            return;
        }

        Users taskAgent = assignedAgent != null ? assignedAgent : application.getAssignedAgent();
        if (taskAgent == null || taskAgent.getId() == null) {
            return;
        }

        LocalDateTime deadline = resolveCollectionDeadline(nextEmi.getDueDate());

        AgentTask task = AgentTask.builder()
                .taskCode("TSK-COL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .loanApplication(application)
                .agent(taskAgent)
                .assignedBy(assignedAgent)
                .taskType(AgentTaskType.CASH_COLLECTION)
                .taskStatus(TaskStatus.ASSIGNED)
                .taskDescription("Collect EMI installments from borrower as per schedule.")
                .priorityLevel("MEDIUM")
                .deadline(deadline)
                .otpRequired(true)
                .build();
        agentTaskRepository.save(task);
    }

    private LocalDateTime resolveCollectionDeadline(LocalDate dueDate) {
        if (dueDate == null) {
            return LocalDateTime.now().plusHours(72);
        }
        LocalDateTime dueEndOfDay = dueDate.atTime(23, 59, 59);
        return dueEndOfDay.isAfter(LocalDateTime.now()) ? dueEndOfDay : LocalDateTime.now().plusHours(24);
    }
}

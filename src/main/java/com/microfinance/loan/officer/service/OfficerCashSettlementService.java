package com.microfinance.loan.officer.service;

import com.microfinance.loan.common.enums.CashSettlementStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.officer.dto.request.CashSettlementRequest;
import com.microfinance.loan.officer.dto.response.CashSettlementResponse;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.payment.entity.Payment;
import com.microfinance.loan.payment.repository.TransactionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfficerCashSettlementService {

    private final CurrentUserService currentUserService;
    private final OfficerProfileRepository officerProfileRepository;
    private final TransactionRepository transactionRepository;

    public OfficerCashSettlementService(CurrentUserService currentUserService,
                                        OfficerProfileRepository officerProfileRepository,
                                        TransactionRepository transactionRepository) {
        this.currentUserService = currentUserService;
        this.officerProfileRepository = officerProfileRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public CashSettlementResponse settleCash(Authentication authentication, CashSettlementRequest request) {
        Long officerUserId = currentUserService.getCurrentUserId(authentication);
        OfficerProfile officer = officerProfileRepository.findByUsersIdWithBranch(officerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Officer profile not found for user: " + officerUserId));

        if (officer.getBranchProfile() == null) {
            throw new IllegalArgumentException("Officer is not mapped to any branch");
        }

        Set<Long> uniqueIds = request.getPaymentIds().stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("At least one valid payment id is required");
        }

        String branchCode = officer.getBranchProfile().getBranchCode();
        List<Payment> payments = transactionRepository.findBranchUnsettledCashPaymentsByIds(List.copyOf(uniqueIds), branchCode);

        Set<Long> foundIds = payments.stream().map(Payment::getId).collect(Collectors.toSet());
        List<Long> invalidIds = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!invalidIds.isEmpty()) {
            throw new IllegalArgumentException("Some payments are not eligible for settlement in this branch: " + invalidIds);
        }

        LocalDateTime settledAt = LocalDateTime.now();
        String settlementReference = StringUtils.hasText(request.getSettlementReference())
                ? request.getSettlementReference().trim()
                : generateSettlementReference();

        double settledAmount = 0d;
        for (Payment payment : payments) {
            payment.setSettlementStatus(CashSettlementStatus.SETTLED);
            payment.setSettledAt(settledAt);
            payment.setSettledBy(officer.getUsers());
            payment.setSettlementReference(settlementReference);
            settledAmount += safeAmount(payment.getTotalPaidAmount());
        }

        transactionRepository.saveAll(payments);

        return CashSettlementResponse.builder()
                .settledPaymentsCount(payments.size())
                .settledAmount(round(settledAmount))
                .settlementReference(settlementReference)
                .settledAt(settledAt)
                .settledPaymentIds(payments.stream().map(Payment::getId).toList())
                .build();
    }

    private String generateSettlementReference() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "SETTLE-" + ts + "-" + suffix;
    }

    private double safeAmount(Double amount) {
        return amount == null ? 0d : amount;
    }

    private double round(double value) {
        return Math.round(value * 100d) / 100d;
    }
}


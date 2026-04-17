package com.microfinance.loan.officer.service;

import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.CashSettlementStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.officer.dto.request.CashSettlementRequest;
import com.microfinance.loan.officer.dto.response.CashSettlementResponse;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.payment.entity.Payment;
import com.microfinance.loan.payment.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerCashSettlementServiceTest {

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private OfficerProfileRepository officerProfileRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private OfficerCashSettlementService officerCashSettlementService;

    @Test
    void settleCash_shouldMarkAllPaymentsAsSettled() {
        Users officerUser = Users.builder().id(51L).build();
        BranchProfile branch = BranchProfile.builder().branchCode("BR001").build();
        OfficerProfile officer = OfficerProfile.builder().users(officerUser).branchProfile(branch).build();

        Payment p1 = Payment.builder().id(10L).totalPaidAmount(500.0).settlementStatus(CashSettlementStatus.COLLECTED_UNSETTLED).build();
        Payment p2 = Payment.builder().id(11L).totalPaidAmount(650.0).settlementStatus(CashSettlementStatus.COLLECTED_UNSETTLED).build();

        CashSettlementRequest request = new CashSettlementRequest();
        request.setPaymentIds(List.of(10L, 11L));
        request.setSettlementReference("DEP-BR001-001");

        when(currentUserService.getCurrentUserId(authentication)).thenReturn(51L);
        when(officerProfileRepository.findByUsersIdWithBranch(51L)).thenReturn(Optional.of(officer));
        when(transactionRepository.findBranchUnsettledCashPaymentsByIds(anyList(), any())).thenReturn(List.of(p1, p2));

        CashSettlementResponse response = officerCashSettlementService.settleCash(authentication, request);

        assertEquals(2, response.getSettledPaymentsCount());
        assertEquals(1150.0, response.getSettledAmount());
        assertEquals("DEP-BR001-001", response.getSettlementReference());
        assertTrue(response.getSettledAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Payment>> captor = (ArgumentCaptor<Iterable<Payment>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);
        verify(transactionRepository).saveAll(captor.capture());
        List<Payment> savedPayments = new ArrayList<>();
        captor.getValue().forEach(savedPayments::add);

        assertEquals(CashSettlementStatus.SETTLED, savedPayments.get(0).getSettlementStatus());
        assertEquals(CashSettlementStatus.SETTLED, savedPayments.get(1).getSettlementStatus());
        assertEquals(officerUser, savedPayments.get(0).getSettledBy());
        assertEquals(officerUser, savedPayments.get(1).getSettledBy());
    }

    @Test
    void settleCash_shouldFailWhenAnyPaymentIsNotEligible() {
        Users officerUser = Users.builder().id(61L).build();
        BranchProfile branch = BranchProfile.builder().branchCode("BR009").build();
        OfficerProfile officer = OfficerProfile.builder().users(officerUser).branchProfile(branch).build();

        Payment eligible = Payment.builder().id(90L).totalPaidAmount(200.0).build();
        CashSettlementRequest request = new CashSettlementRequest();
        request.setPaymentIds(List.of(90L, 91L));

        when(currentUserService.getCurrentUserId(authentication)).thenReturn(61L);
        when(officerProfileRepository.findByUsersIdWithBranch(61L)).thenReturn(Optional.of(officer));
        when(transactionRepository.findBranchUnsettledCashPaymentsByIds(anyList(), any())).thenReturn(List.of(eligible));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> officerCashSettlementService.settleCash(authentication, request));

        assertTrue(ex.getMessage().contains("not eligible"));
    }
}




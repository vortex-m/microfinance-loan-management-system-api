package com.microfinance.loan.loan.repository;

import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanEmiScheduleRepository extends JpaRepository<LoanEmiSchedule, Long> {
    List<LoanEmiSchedule> findByLoanIdOrderByEmiNumberAsc(Long loanId);

    Optional<LoanEmiSchedule> findByLoanIdAndEmiNumber(Long loanId, Integer emiNumber);

    Optional<LoanEmiSchedule> findFirstByLoanIdAndEmiStatusOrderByEmiNumberAsc(Long loanId, String emiStatus);

    Optional<LoanEmiSchedule> findFirstByLoanIdAndEmiStatusInOrderByEmiNumberAsc(Long loanId, Collection<String> emiStatuses);

    long countByLoanIdAndEmiStatus(Long loanId, String emiStatus);

    List<LoanEmiSchedule> findByDueDateBeforeAndEmiStatus(LocalDate dueDate, String emiStatus);
}


package com.microfinance.loan.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum ManagerDepartment {
    LOAN_OPERATIONS,
    CREDIT_UNDERWRITING,
    KYC_COMPLIANCE,
    COLLECTIONS_RECOVERY,
    BRANCH_OPERATIONS,
    AUDIT_FRAUD_CONTROL;

    public static Optional<ManagerDepartment> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        return Arrays.stream(values())
                .filter(department -> department.name().equals(normalized))
                .findFirst();
    }
}


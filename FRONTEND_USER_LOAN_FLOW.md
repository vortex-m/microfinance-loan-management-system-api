# Frontend User Loan Flow Guide

This document maps the user-side navigation to backend APIs and UI requirements.

Base URL: `/api`

Standard response wrapper for all endpoints:

```json
{
  "success": true,
  "message": "...",
  "data": {},
  "timestamp": "2026-04-19T18:00:00"
}
```

Authentication header:

`Authorization: Bearer <userToken>`

---

## 1) End-to-End Loan Apply and Verification Flow

1. User submits loan application.
2. Officer reviews and assigns agent for field verification.
3. Agent completes verification report (+ image evidence).
4. Officer approves -> status becomes `PENDING_MANAGER_APPROVAL`.
5. Manager approves -> loan record is created (`loanId`, EMI schedule created).
6. Disbursal:
   - `BANK_TRANSFER`: manager confirms bank disbursal.
   - `CASH`: agent generates and verifies disbursal OTP.
7. After disbursal, user can view EMI schedule and pay EMI.

Typical status progression:

`PENDING -> UNDER_REVIEW -> PENDING_MANAGER_APPROVAL -> APPROVED -> DISBURSED -> CLOSED`

---

## 2) User Navigation Mapping

## Apply Loan

UI route: `/user/loans/apply`

### API routes
- `POST /users/loans/bank-proof/upload` (optional pre-step for bank mode)
- `POST /users/loans/apply`

### UI inputs
- `requestedAmount` (number, required, 1000-200000)
- `tenureMonths` (number, required)
- `loanPurpose` (text, required)
- `loanPurposeDescription` (text, optional)
- `userRemarks` (text, required)
- `disbursalMode` (`BANK_TRANSFER` or `CASH`, required)
- Bank fields when `disbursalMode = BANK_TRANSFER`:
  - `disbursalBankName`
  - `disbursalBankAccount`
  - `disbursalIfscCode`
  - `disbursalBankProofUrl`
  - `disbursalBankProofFileName`

### Request example
```json
{
  "requestedAmount": 30000,
  "tenureMonths": 12,
  "loanPurpose": "Livestock",
  "loanPurposeDescription": "Milk business expansion",
  "userRemarks": "Need support before season",
  "disbursalMode": "CASH"
}
```

### Data to show after submit (`LoanApplyResponse`)
- `loanApplicationId`
- `applicationNumber`
- `status`
- `originChannel`
- `requestedAmount`
- `tenureMonths`
- `loanPurpose`
- `disbursalMode`
- `assignedAgentId`, `assignedOfficerId`
- `appliedAt`

---

## My Loans

UI route: `/user/loans`

### API route
- `GET /users/loans`

### Data to render (`LoanStatusResponse.loans[]`)
- `loanApplicationId`
- `applicationNumber`
- `loanId` (null before manager approval)
- `loanNumber`
- `requestedAmount`
- `approvedAmount`
- `tenureMonths`
- `loanPurpose`
- `disbursalMode`
- `status`
- `emiAmount`
- `totalPaidAmount`
- `outstandingPrincipal`
- `nextDueDate`
- `rejectionReason`
- `appliedAt`, `updatedAt`

UI notes:
- Show status badge.
- Hide EMI actions until `loanId` exists and loan is disbursed.

---

## Loan Details

UI route: `/user/loans/:loanApplicationId`

### API route
- `GET /users/loans/{loanApplicationId}`

### Data to render (`LoanDetailResponse`)
Application block:
- `loanApplicationId`, `applicationNumber`, `status`
- `requestedAmount`, `tenureMonths`, `loanPurpose`, `loanPurposeDescription`, `userRemarks`
- `disbursalMode`, `disbursalBankName`, `disbursalBankAccountMasked`, `disbursalIfscCode`

Approval/loan block:
- `loanNumber`
- `approvedAmount`
- `interestRate`, `interestType`
- `emiAmount`, `processingFee`
- `totalInterestPayable`, `totalAmountPayable`

Repayment summary:
- `totalEmis`, `emisPaid`, `emisPending`, `emisOverdue`
- `outstandingPrincipal`, `totalPaidAmount`, `pendingAmount`, `nextDueDate`

Dates and assignment:
- `disbursementDate`, `firstEmiDate`, `lastEmiDate`, `disbursedAt`
- `assignedAgentName`, `assignedAgentPhone`
- `assignedOfficerName`, `assignedOfficerPhone`
- `officerRemarks`, `rejectionReason`

---

## EMI Schedule

UI route: `/user/loans/:loanId/emi-schedule`

### API route
- `GET /users/loans/{loanId}/emi-schedule`

### Data to render (`EmiScheduleResponse`)
Top summary:
- `loanNumber`, `principalAmount`, `interestRate`, `totalEmis`, `emiAmount`

EMI list rows (`schedule[]`):
- `emiScheduleId`
- `emiNumber`
- `dueDate`
- `emiAmount`
- `principalComponent`
- `interestComponent`
- `outstandingPrincipal`
- `emiStatus` (`PENDING`, `PAID`, `OVERDUE`, `PARTIALLY_PAID`)
- `paidAmount`, `paidDate`
- `penaltyAmount`, `daysOverdue`

---

## Pay EMI

UI route: `/user/loans/:loanId/pay`

### API route
- `POST /users/loans/{loanId}/emi/pay`

### UI inputs (`EmiPayRequest`)
- `emiScheduleId` (required)
- `paymentAmount` (required)
- `paymentMode` (required)
- `gatewayOrderId` (optional)
- `paymentReference` (optional; backend auto-generates if missing)

### Request example
```json
{
  "emiScheduleId": 11,
  "paymentAmount": 2500,
  "paymentMode": "BANK_TRANSFER",
  "gatewayOrderId": "ORDER-1",
  "paymentReference": "REF-ABC-1"
}
```

### Response to render (`PaymentHistoryResponse.PaymentItem`)
- `paymentId`, `paymentNumber`
- `loanId`, `loanNumber`
- `emiScheduleId`, `emiNumber`
- `totalPaidAmount`, `principalPaid`, `interestPaid`, `penaltyPaid`
- `paymentMode`, `paymentStatus`
- `paymentReference`, `gatewayTransactionId`
- `cashSettlementStatus`, `cashVerifiedAt`, `settledAt`
- `paidAt`, `receiptNumber`

Important behavior:
- User endpoint blocks `CASH` mode direct payment.
- Cash EMI is handled through agent OTP collection flow.

---

## Payment History

UI route: `/user/loans/:loanId/payments` and `/user/payments`

### API routes
- `GET /users/loans/{loanId}/payments` (loan-wise)
- `GET /users/loans/payments` (all loans)

### Data to render (`PaymentHistoryResponse.payments[]`)
- `paymentNumber`, `paidAt`, `paymentStatus`
- `loanNumber`, `emiNumber`
- `totalPaidAmount`
- split: `principalPaid`, `interestPaid`, `penaltyPaid`
- `paymentMode`, `paymentReference`, `receiptNumber`
- `cashSettlementStatus` (for cash collection visibility)

---

## 3) UX Conditions and Guards

- Disable EMI schedule/pay/history buttons when:
  - `loanId` is null, or
  - loan not yet disbursed.
- Show rejection box when `status = REJECTED` with `rejectionReason`.
- For bank transfer apply mode, enforce bank fields before submit.
- For cash disbursal mode, show note: disbursal is completed by agent OTP verification.

---

## 4) Recommended Frontend Data Model Keys

Keep these IDs in state:
- `loanApplicationId` for application/detail screens
- `loanId` for EMI and payment screens
- `emiScheduleId` for pay action

---

## 5) Minimal Postman Smoke Sequence (User)

1. `POST /users/loans/apply`
2. `GET /users/loans`
3. `GET /users/loans/{loanApplicationId}`
4. (After disbursal) `GET /users/loans/{loanId}/emi-schedule`
5. `POST /users/loans/{loanId}/emi/pay`
6. `GET /users/loans/{loanId}/payments`


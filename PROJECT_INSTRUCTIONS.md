Microfinance Loan Management System — Project Instructions

Purpose

This document is a complete, practical instruction and reference file for developers, maintainers and operators working on the Loan Management System API in this repository. It consolidates setup, architecture, entities, workflows, common developer tasks, debugging tips and operational notes so contributors can get started quickly and understand how the system is intended to behave.

Location

The canonical project source and instructions are under the repository root. Relevant files you will need to reference:

- `pom.xml` — Maven build
- `src/main/resources/application.yml` and `application-dev.yml` — configuration
- `src/main/java/com/microfinance/loan` — main sources
- `src/test` — tests
- `upload/` — sample uploads used by tests or development

High-level plan

I will:
- Provide quick start and dev setup steps
- Describe configuration and environment variables for each profile
- Document the expected domain entities and relationships
- Describe the recommended loan workflow (agent -> officer -> manager) and role responsibilities
- Provide API usage examples and notes on security
- Add developer troubleshooting section including a known compile error in `AiController.java` and how to fix it
- Provide testing, logging and deployment notes

Checklist (what this file covers)

- [x] Development prerequisites and quick-start
- [x] Running the app locally with different profiles
- [x] DB configuration (dev / test)
- [x] Entities list and recommended model completeness
- [x] Loan lifecycle workflow (apply, verify, approve, disburse)
- [x] REST API roots and examples
- [x] Common issues and fixes (including `AiController.java` fix)
- [x] Testing and CI hints
- [x] Contribution and code style guidance

1) Quick start — prerequisites

- Java 17 (JDK 17+) installed and JAVA_HOME set
- Maven 3.8+ (you can use the project's Maven Wrapper `mvnw.cmd` on Windows)
- MySQL 8+ (for `dev` profile) or a local Docker container for the DB

2) Clone, build and run locally (Windows PowerShell)

```powershell
# clone (if not already present)
git clone <your-repo-url> loan-management-system-api
Set-Location .\loan-management-system-api

# run using the bundled Maven wrapper (dev profile is default)
.\mvnw.cmd spring-boot:run

# or build artifact then run
.\mvnw.cmd clean package
java -jar .\target\loan-management-system-api-0.0.1-SNAPSHOT.jar
```

To explicitly set the Spring profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

3) Configuration and environment variables

Files
- `src/main/resources/application.yml` — common properties and default active profile
- `src/main/resources/application-dev.yml` — development profile (MySQL)
- `src/test/resources/application-test.yml` — test profile (H2 in-memory)

Important env variables (used by `application-dev.yml`)
- DB_URL — JDBC URL, e.g. jdbc:mysql://localhost:3306/loan_db?useSSL=false&serverTimezone=UTC
- DB_USERNAME — database user
- DB_PASSWORD — database password

Recommended local Docker MySQL run (example):

```powershell
docker run --name loan-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=loan_db -e MYSQL_USER=loan_user -e MYSQL_PASSWORD=loan_pass -p 3306:3306 -d mysql:8
```

4) Project structure and important packages

Root: `src/main/java/com/microfinance/loan`
- `auth` — authentication and token management
- `users` — user registration, profiles
- `agent` — field agents responsibilities (verification tasks, collection)
- `officer` — loan officers (loan reviewing and approval)
- `manager` — manager operations and overrides
- `loan` — loan domain, applications, disbursement
- `payment` — repayments, schedules, transactions
- `ai` — AI-assisted features (credit scoring)
- `common` — shared DTOs, ApiResponse, utility code
- `config` — security and app configuration

5) Entities — current completeness and recommended fields

This project scaffolds many domain modules. Below is a recommended set of core entities you should have (or confirm exist). If any of these are missing in code, they should be added.

- User
  - id (Long)
  - username / email
  - passwordHash
  - roles (ADMIN, MANAGER, OFFICER, AGENT, CUSTOMER)
  - profile fields (firstName, lastName, phone, address)
  - status (ACTIVE, INACTIVE/LOCKED)

- Agent
  - id
  - userId -> User
  - areas/assignedClients
  - status

- Officer
  - id
  - userId -> User
  - assignedBranch

- Manager
  - id
  - userId -> User
  - oversightBranches

- LoanApplication (core)
  - id
  - applicantId (User)
  - principalAmount
  - currency
  - termMonths
  - interestRate
  - productType
  - applicationDate
  - status (DRAFT, SUBMITTED, VERIFIED_BY_AGENT, VERIFIED_BY_OFFICER, APPROVED, REJECTED, DISBURSED, CLOSED)
  - assignedAgentId
  - assignedOfficerId
  - verificationNotes
  - decisionReason
  - approvedAmount
  - disbursementDate

- LoanAccount (active loan)
  - id
  - loanApplicationId
  - outstandingBalance
  - scheduleId
  - status

- RepaymentSchedule / Installment
  - id
  - loanAccountId
  - dueDate
  - principalDue
  - interestDue
  - status (PENDING, PAID, LATE)

- PaymentTransaction
  - id
  - loanAccountId
  - amount
  - method
  - transactionDate
  - status

- CreditScore or RiskProfile (for `ai` module)
  - userId
  - score
  - lastUpdated

- FollowUpTask / VerificationTask
  - id
  - createdBy (agent/officer/system)
  - assignedTo
  - type (FIELD_VERIFICATION, DOCUMENT_COLLECTION, OVERDUE_FOLLOWUP)
  - status (OPEN, IN_PROGRESS, DONE)

Notes on completeness
- The project needs explicit status transitions for `LoanApplication` (see workflow below). If you find statuses missing, add them so the state machine is deterministic.
- Keep references via foreign keys (or JPA relationships) and avoid embedding heavy data in a single table.

6) Loan lifecycle workflow (recommended)

A recommended flow that matches your earlier questions (agent -> officer -> approval):

1. Customer submits `LoanApplication` (status SUBMITTED).
2. System assigns an `Agent` automatically or manually (assignedAgentId).
3. Agent performs field verification and collects required documents. Agent marks the application as VERIFIED_BY_AGENT and adds verification notes; may create `FollowUpTask`s if more info needed.
4. Once agent verification is complete, the application moves to the `Officer` queue (status SUBMITTED or VERIFIED_BY_AGENT depending on design). Assigned officer reviews the application and supporting documents.
5. Officer can:
   - approve the application -> status APPROVED (record approvedAmount and interest/term adjustments)
   - reject the application -> status REJECTED (record reason)
   - request more info -> create FollowUpTask and set status back to SUBMITTED or a special NEEDS_INFO status
6. On approval, the Manager may optionally be required to authorize (depending on thresholds). If manager sign-off is mandatory, move to PENDING_MANAGER_APPROVAL before final APPROVED.
7. After final approval, the disbursement process creates a `LoanAccount` and sets status to DISBURSED once funds are transferred.
8. Repayment schedules are created and tracked; payments create `PaymentTransaction` entries and update installments' statuses.

Authorization model
- Roles and route access should map to the workflow responsibilities:
  - AGENT: create/verify applications, update verification notes, create tasks
  - OFFICER: review, request info, approve/reject loans
  - MANAGER: oversee approvals and manually override where needed
  - ADMIN: manage users and system config

7) REST API overview and conventions

- Base context: configured in `application-dev.yml` as `/api` by default
- Common route roots (scaffolded):
  - `/auth` – login, token, refresh
  - `/users` – user CRUD
  - `/agents` – agent operations
  - `/officers` – officer operations
  - `/loans` – loan applications, approvals, disbursement
  - `/payments` – repayment and transactions
  - `/ai` – AI endpoints (credit scoring)

API return type
- The project uses a common `ApiResponse<T>` wrapper (found in `com.microfinance.loan.common.dto`) to standardize success/error messages.

Example: refresh credit score (AI)
- POST `/api/ai/scores/users/{userId}/refresh`
  - Roles allowed: ADMIN, MANAGER, OFFICER
  - Optional body: `{ "createFollowUpTask": true }`
  - Response: `ApiResponse<CreditScoreResponse>`

8) Security

- Spring Security is configured in `src/main/java/com/microfinance/loan/config` (check classes there for JWT and role mapping)
- Protect endpoints using `@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")` or similar expressions
- Ensure password storage uses a secure password encoder (BCryptPasswordEncoder)

9) Tests

- Unit and integration tests are under `src/test/java`
- The test profile uses H2 in-memory DB (`application-test.yml`) and `create-drop` schema for isolation
- Run tests locally:

```powershell
.\mvnw.cmd test
```

10) Logging

- Logging config: `src/main/resources/logback.xml` and properties in the YAML profiles
- Application log file present at project root `loanManagement.log` when run locally (check `application-dev.yml` for exact path)

11) Troubleshooting — common compile / runtime issues

A. Malformed controller class: `AiController.java`

If you see compiler errors or the app fails to start because of `AiController.java`, the problem in the repository is that annotations and imports are misplaced and the class body is closed prematurely. Example symptoms:
- "class, interface, or enum expected"
- "illegal start of type"
- Spring fails to start due to invalid bean definitions

How to fix (summary):
- Ensure all imports appear before annotations or class declarations
- Remove stray `}` that prematurely closes the class
- Place `@RestController` and `@RequestMapping` immediately above the class definition

Minimal corrected example for `AiController.java` (conceptual, adapt exact package imports):

```java
package com.microfinance.loan.ai.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
// ... other imports ...

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // ... handler methods inside the class ...
}
```

If you want, I can patch this file automatically — I can update the repository file to the correct structure and run a quick build to validate.

B. Database connection issues

- Verify `DB_URL` and credentials; if the schema doesn't exist, either create it or change JPA settings in `application-dev.yml` (DDL `update` vs `create`)
- When using Docker, confirm container is running and port mapping is correct

C. Missing environment variables

- The app will fail to start if required properties are missing. Consider adding default fallbacks in `application-dev.yml` for local development or set the env variables before running the app.

12) Contribution guidelines

- Follow existing package and naming conventions
- Keep controllers thin; place business logic in `service` classes
- Write unit tests for new services and controllers
- Use Lombok for DTOs and simple entities if the project already uses it, but prefer explicit getters/setters in complex domain classes
- When adding DB migrations, prefer Flyway or Liquibase (not currently present in project) and add migration scripts under `src/main/resources/db/migration`

13) Suggested next technical improvements (roadmap)

- Add OpenAPI/Swagger docs (springdoc-openapi)
- Add DB migrations (Flyway) to maintain schema changes
- Implement event-based notifications (email/SMS) for important loan lifecycle events
- Add more granular audit logs for sensitive actions (approvals, money transfer)
- Implement rate-limiting and request validation for public endpoints

14) Operational notes for QA and staging

- Use a separate `staging` profile with its database and feature toggles
- Seed test data for agent/officer/manager accounts and sample loan applications
- Use the `upload/` dir for test attachments; consider moving to a blob store in production

15) Where to get help

- Project logs in `loanManagement.log` and files in `target/surefire-reports` for test failures
- Contact the code owner / project maintainer when available

Appendix A — Example API calls

1) Submit a loan application (customer)

POST /api/loans
Headers: Authorization: Bearer <token>
Body (example):

{
  "applicantId": 123,
  "principalAmount": 10000,
  "termMonths": 12,
  "interestRate": 12.5,
  "productType": "PERSONAL"
}

2) Agent verifies application

POST /api/agents/verify-loan/{loanId}
Body:
{
  "status": "VERIFIED_BY_AGENT",
  "notes": "documents verified, field visit done"
}

3) Officer approves application

POST /api/officers/approve/{loanId}
Body:
{
  "status": "APPROVED",
  "approvedAmount": 9500,
  "decisionReason": "minor doc discrepancy resolved"
}

Appendix B — If you want me to patch code

I can automatically fix small issues (for example the `AiController.java` class structure) and run a Maven build to confirm there are no compile errors. Tell me if you want me to:

- [ ] Patch `AiController.java` to correct placement of annotations and class body
- [ ] Run `.\mvnw.cmd -DskipTests clean package` and share the output

If you'd like these automated edits and quick verification, reply with which actions to take and I'll proceed.

---

File created: `PROJECT_INSTRUCTIONS.md`

If you want, I will now:
- patch `AiController.java` and run a quick build to demonstrate everything compiles, or
- open a PR-style patch you can review before applying.

Which of these would you like me to do next?

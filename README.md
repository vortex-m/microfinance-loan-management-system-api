# Microfinance Loan Management System API

A Spring Boot backend for microfinance loan operations, including users and role workflows, loan lifecycle handling, and repayment tracking.

This repository is currently in an early stage: module structure and route scaffolding are in place, with core business APIs being implemented incrementally.

## Table of Contents

- [Project Description](#project-description)
- [Current Status](#current-status)
- [Tech Stack](#tech-stack)
- [Module Overview](#module-overview)
- [Project Structure](#project-structure)
- [API Usage](#api-usage)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Run the Project](#run-the-project)
- [Run Tests](#run-tests)
- [Roadmap](#roadmap)

## Project Description

This API is intended to support small lending organizations by centralizing day-to-day loan operations:

- customer and staff account management
- loan application and approval flow
- repayment tracking and payment records
- role-based access control
- extensible modules for AI-assisted features

## Current Status

What is currently available in code:

- Spring Boot project setup with Java 17 and Maven
- package-wise module separation (`auth`, `loan`, `payment`, `users`, etc.)
- profile-based environment configuration (`dev`, `test`)
- initial controller route scaffolding for major modules
- working health-check endpoint

What is in progress:

- business logic and CRUD endpoints across modules
- stronger authorization and validation flow
- full workflow coverage for loans and payments

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- Maven
- MySQL (dev)
- H2 (test)
- Lombok

## Module Overview

Main domain packages under `src/main/java/com/microfinance/loan`:

- `auth` - authentication and authorization APIs (scaffolded)
- `users` - users-related APIs (scaffolded)
- `manager` - manager operations (scaffolded)
- `officer` - loan officer operations (scaffolded)
- `agent` - field agent operations (scaffolded)
- `loan` - loan lifecycle APIs (scaffolded)
- `payment` - repayment/payment APIs (scaffolded)
- `ai` - AI-related extensions (scaffolded)
- `common` - shared concerns and health endpoint
- `config` - security and app configuration

## Project Structure

```text
loan-management-system-api/
|-- pom.xml
|-- README.md
`-- src/
    |-- main/
    |   |-- java/com/microfinance/loan/
    |   |   |-- agent/
    |   |   |-- ai/
    |   |   |-- auth/
    |   |   |-- common/
    |   |   |-- config/
    |   |   |-- exception/
    |   |   |-- loan/
    |   |   |-- manager/
    |   |   |-- officer/
    |   |   |-- payment/
    |   |   `-- users/
    |   `-- resources/
    |       |-- application.yml
    |       |-- application-dev.yml
    |       `-- logback.xml
    `-- test/
        |-- java/com/microfinance/loan/
        `-- resources/application-test.yml
```

## API Usage

Base path is `/api`.

Default local base URL (`dev`):

- `http://localhost:8080/api`

Available endpoint:

- `GET /health-check`
- full URL: `http://localhost:8080/api/health-check`
- response: `OK`

Scaffolded route roots currently present in controllers:

- `/auth`
- `/users`
- `/manager`
- `/officers`
- `/agents`
- `/loans`
- `/payments`
- `/ai`

## Prerequisites

- JDK 17+
- Maven 3.9+ (or use bundled Maven Wrapper)
- MySQL 8+ for `dev` profile runs

## Configuration

Default profile (`src/main/resources/application.yml`):

- `spring.profiles.active=dev`

### Development (`application-dev.yml`)

- Port: `8080`
- Context path: `/api`
- MySQL datasource with env-variable override support:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
- JPA DDL mode: `update`

### Test (`src/test/resources/application-test.yml`)

- In-memory H2 database
- JPA DDL mode: `create-drop`

## Run the Project

Clone and start locally (Windows PowerShell):

```powershell
git clone https://github.com/vortex-m/microfinance-loan-management-system-api.git
Set-Location .\microfinance-loan-management-system-api
.\mvnw.cmd spring-boot:run
```

Run with explicit profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Build artifact:

```powershell
.\mvnw.cmd clean package
```

Run the packaged JAR:

```powershell
java -jar .\target\loan-management-system-api-0.0.1-SNAPSHOT.jar
```

## Run Tests

```powershell
.\mvnw.cmd test
```

## Roadmap

Planned next milestones:

- implement CRUD APIs across domain modules
- complete loan approval/disbursement workflows
- implement repayment schedule and transaction history
- harden role-based security and authorization
- add API documentation (OpenAPI/Swagger)
- integrate notifications and AI-assisted risk checks
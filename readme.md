# Multi-Tenant Banking & Cooperative Finance System

A production-grade cooperative and microfinance banking backend built with Spring Boot, PostgreSQL, JWT authentication, transactional financial operations, and Clean Architecture principles.

---

# Table of Contents

1. Overview
2. Features
3. Core Concepts
4. Architecture
5. Technology Stack
6. System Design
7. Entity Relationship Design
8. Authentication & Authorization
9. Multi-Tenancy
10. Financial Engine
11. Loan Management
12. Scheduled Jobs
13. Reporting
14. Database Design
15. API Endpoints
16. Project Structure
17. Installation & Setup
18. Environment Variables
19. Running the Application
20. Testing
21. Security
22. Transaction Management
23. Concurrency Handling
24. Business Rules
25. Future Improvements

---

# 1. Overview

This project is a production-grade REST API platform for managing:

* Cooperative societies
* Microfinance institutions
* Members
* Savings accounts
* Loans
* Transactions
* Interest accruals
* Audit logs

The system supports multiple institutions (multi-tenancy) while ensuring strict financial correctness, transactional integrity, auditability, and institution-level data isolation.

---

# 2. Features

## Authentication & Authorization

* JWT authentication
* Role-based access control
* Refresh tokens
* Change password
* Institution-scoped access

---

## Institution Management

* Register institutions
* Approve/suspend institutions
* Institution financial summaries
* Platform-wide statistics

---

## Member Management

* Register members
* Member profile management
* Automatic savings account creation

---

## Savings Management

* Deposit
* Withdrawal
* Transfer
* Fixed savings enforcement
* Target savings accounts
* Savings interest accrual

---

## Loan Management

* Loan products
* Loan applications
* Loan approval workflow
* Loan disbursement
* Repayment schedules
* Loan repayments
* Default handling
* Loan write-offs

---

## Transactions

* Full transaction history
* Reversals
* Double-entry bookkeeping principles
* Immutable transaction references

---

## Reports

* Loan portfolio reports
* Institution financial summaries
* Member statements
* Overdue loan reports

---

## Scheduled Jobs

* Monthly interest accrual
* Daily overdue loan detection
* Automatic loan defaulting

---

# 3. Core Concepts

## Multi-Tenancy

Each institution operates independently.

Institution A cannot access Institution B’s data.

All queries are scoped using:

```java id="h5u4xb"
institutionId
```

---

## Transactional Integrity

Every financial operation is atomic.

If one step fails:

* the entire transaction rolls back.

Implemented using:

```java id="p4n7sv"
@Transactional
```

---

## Financial Correctness

The system prevents:

* negative balances
* concurrent balance corruption
* inconsistent repayments

---

## Auditability

Every sensitive operation is logged.

Examples:

* loan approval
* account freezing
* transaction reversal
* loan write-off

---

# 4. Architecture

The project follows Clean Architecture principles.

```text id="0mdrf7"
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

---

# 5. Technology Stack

| Technology      | Purpose                        |
| --------------- | ------------------------------ |
| Java 21         | Programming Language           |
| Spring Boot 3   | Backend Framework              |
| Spring Security | Authentication & Authorization |
| JWT             | Stateless Authentication       |
| Spring Data JPA | ORM                            |
| Hibernate       | Persistence                    |
| PostgreSQL      | Database                       |
| Flyway          | Database Migration             |
| Lombok          | Boilerplate Reduction          |
| MapStruct       | DTO Mapping                    |
| JUnit 5         | Unit Testing                   |
| Mockito         | Mocking                        |
| Swagger/OpenAPI | API Documentation              |

---

# 6. System Design

The application is modular.

```text id="q6j0ur"
auth
institution
user
member
savings
transaction
loan
audit
report
scheduler
security
config
exception
util
```

---

# 7. Entity Relationship Design

# Main Relationships

```text id="mfvab8"
Institution
│
├── Users
│      └── MemberProfile
│              ├── SavingsAccounts
│              │       └── Transactions
│              │
│              └── LoanApplications
│                      ├── LoanRepaymentSchedules
│                      ├── LoanGuarantors
│                      └── LoanCollaterals
│
├── LoanProducts
│
└── AuditLogs
```

---

# 8. Authentication & Authorization

# JWT Payload

```json id="6e3iz9"
{
  "userId": 1,
  "institutionId": 3,
  "role": "ACCOUNTANT"
}
```

---

# Roles

| Role              | Permissions                  |
| ----------------- | ---------------------------- |
| SUPER_ADMIN       | Platform-wide administration |
| INSTITUTION_ADMIN | Institution management       |
| LOAN_OFFICER      | Loan reviews                 |
| ACCOUNTANT        | Financial operations         |
| MEMBER            | Self-service access          |

---

# 9. Multi-Tenancy

All data access is scoped using institutionId.

Example:

```java id="tl8d0z"
findByIdAndInstitutionId()
```

NOT:

```java id="kk2r1c"
findById()
```

This prevents cross-institution access.

---

# 10. Financial Engine

The financial engine handles:

* deposits
* withdrawals
* transfers
* disbursements
* repayments
* reversals

---

# Transaction Rules

Every transaction stores:

* balance_before
* balance_after

Balances are never computed retrospectively.

---

# Transaction Reference Format

```text id="i9q8yw"
TXN-{institutionId}-{UUID}
```

---

# Reversal Logic

A reversal creates an opposite transaction.

Example:

| Original   | Reversal   |
| ---------- | ---------- |
| DEPOSIT    | WITHDRAWAL |
| WITHDRAWAL | DEPOSIT    |

---

# 11. Loan Management

# Loan Lifecycle

```text id="9gb6nk"
PENDING
   ↓
UNDER_REVIEW
   ↓
APPROVED
   ↓
DISBURSED
   ↓
FULLY_REPAID
```

OR

```text id="g0f0fn"
DISBURSED
   ↓
DEFAULTED
   ↓
WRITTEN_OFF
```

---

# Loan Interest Types

## Flat Interest

\text{Total Interest} = \frac{\text{Principal} \times \text{Rate} \times \text{Tenure}}{100}

---

## Reducing Balance

EMI = P \times r \times \frac{(1+r)^n}{(1+r)^n - 1}

---

# 12. Scheduled Jobs

# Monthly Savings Interest

Runs on the first day of every month.

```java id="h4q2yf"
@Scheduled(cron = "0 0 0 1 * *")
```

Tasks:

* compute interest
* credit savings account
* create transaction
* create accrual record

---

# Daily Loan Monitoring

Runs daily.

```java id="bt9s4e"
@Scheduled(cron = "0 0 1 * * *")
```

Tasks:

* mark overdue installments
* detect defaults
* update loan statuses
* create audit logs

---

# 13. Reporting

# Available Reports

* Total institution savings
* Total outstanding loans
* Total overdue loans
* Collection rate
* Member account statements

Reports are generated primarily via SQL aggregations.

---

# 14. Database Design

# Key Constraints

## Prevent Negative Balances

```sql id="04n6xh"
CHECK (balance >= 0)
```

---

## Unique Account Numbers

```sql id="0h7p6r"
UNIQUE(account_number)
```

---

## Unique Transaction References

```sql id="cx4g5k"
UNIQUE(reference)
```

---

# 15. API Endpoints

# Authentication

```http id="jlwmq8"
POST /api/v1/auth/register-institution
POST /api/v1/auth/login
POST /api/v1/auth/refresh-token
POST /api/v1/auth/change-password
```

---

# Savings

```http id="h9y4za"
POST /api/v1/accounts/deposit
POST /api/v1/accounts/withdraw
POST /api/v1/accounts/transfer
```

---

# Loans

```http id="v3s2mw"
POST /api/v1/loans/apply
POST /api/v1/loans/approve
POST /api/v1/loans/disburse
POST /api/v1/loans/repay
```

---

# Reports

```http id="kn6x9z"
GET /api/v1/reports/portfolio
GET /api/v1/reports/member-statement
```

---

# 16. Project Structure

```text id="t2h7as"
src/main/java/com/cooperative/banking
│
├── auth
├── institution
├── user
├── member
├── savings
├── transaction
├── loan
├── audit
├── report
├── scheduler
├── security
├── config
├── exception
├── util
```

---

# 17. Installation & Setup

# Clone Repository

```bash id="1t5xzd"
git clone <repository-url>
```

---

# Navigate

```bash id="8v2hsa"
cd banking-system
```

---

# Create Database

```sql id="h9w1ap"
CREATE DATABASE cooperative_bank;
```

---

# Run Application

```bash id="x8p3zr"
./mvnw spring-boot:run
```

---

# 18. Environment Variables

# application.yml

```yaml id="a6m8pr"
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cooperative_bank
    username: postgres
    password: password

jwt:
  secret: your-secret-key
  expiration: 86400000
```

---

# 19. Running the Application

# Development Mode

```bash id="18i3yo"
./mvnw spring-boot:run
```

---

# Build JAR

```bash id="mx4g2l"
./mvnw clean package
```

---

# Run JAR

```bash id="s4k6qh"
java -jar target/banking-system.jar
```

---

# 20. Testing

# Run Tests

```bash id="wx3n0k"
./mvnw test
```

---

# Coverage Areas

* deposits
* withdrawals
* transfers
* loan calculations
* repayment allocation
* business rules
* authorization

---

# 21. Security

# Features

* JWT authentication
* BCrypt password hashing
* Stateless sessions
* Role-based authorization
* Institution isolation

---

# 22. Transaction Management

All balance-critical operations use:

```java id="v4r2fc"
@Transactional(
    isolation = Isolation.SERIALIZABLE
)
```

This prevents concurrent balance corruption.

---

# 23. Concurrency Handling

Optimistic locking is implemented on savings accounts.

```java id="4s5v3y"
@Version
private Long version;
```

---

# 24. Business Rules

# Savings Rules

* balance must never go negative
* fixed savings cannot withdraw before maturity
* frozen accounts cannot receive disbursements

---

# Loan Rules

* one active loan at a time
* guarantor required if loan product requires it
* repayment schedules generated atomically

---

# Tenant Rules

* all operations are institution-scoped
* cross-institution access is prohibited

---

# 25. Future Improvements

Potential future enhancements:

* Docker support
* Kubernetes deployment
* Redis caching
* Event-driven architecture
* SMS notifications
* Email notifications
* Two-factor authentication
* API rate limiting
* Account statements PDF export
* Real double-entry ledger engine
* Mobile banking integration
* Payment gateway integration

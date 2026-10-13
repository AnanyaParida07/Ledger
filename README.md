# Budget Tracker

A multi-tenant budgeting application with an Angular frontend and a Spring Boot REST API. Each workspace has its own users, categories, budgets, and transactions.

## Requirements

- Java 21
- PostgreSQL
- Node.js and npm
- Maven, or IntelliJ IDEA with its bundled Maven support

## Configure PostgreSQL

The backend defaults to:

- Database URL: `jdbc:postgresql://localhost:5432/budgettracker`
- Username: `postgres`
- Password: `an1234`

These values can be overridden in PowerShell before starting the backend:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/budgettracker'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<your PostgreSQL password>'
```

The database must have the application's base tables. The checked-in Flyway scripts are incremental migrations and do not create the initial schema. Keep using an already initialized `budgettracker` database; creating an empty database alone is not enough for startup.

For non-local deployments, set `JWT_SECRET` to a private random value of at least 32 bytes:

```powershell
$env:JWT_SECRET = '<private random key>'
```

## Start the backend

Open a terminal in `budget-tracker-backend` and run:

```powershell
mvn spring-boot:run
```

Alternatively, open the backend project in IntelliJ and run the Maven goal `spring-boot:run` or the `BudgetTrackerApplication` main class. The API listens on `http://localhost:8080`.

Wait for the log message `Started BudgetTrackerApplication`. If startup fails, check the first `Caused by` entry. Database connection errors usually mean PostgreSQL is stopped or the `DB_*` settings do not match.

## Start the frontend

In a separate terminal, open `budget-tracker-frontend` and run:

```powershell
npm install
npm start
```

Open `http://localhost:4200`. The development frontend calls `http://localhost:8080/api`; the backend allows requests from `http://localhost:4200`.

## First use

1. Open `http://localhost:4200/register` and create a workspace. Registration creates the workspace and its first user with the `ADMIN` role.
2. The app signs you in automatically. Log in later at `/login` with the same email and password.
3. Create categories, then add transactions and budgets. A new workspace starts with no financial records, so analytics are empty until you add data.

The frontend stores the access token in browser local storage and attaches it to authenticated API requests. If a session expires, log in again.

## Project layout

- `budget-tracker-backend/` — Spring Boot, Spring Security, JPA, PostgreSQL, Flyway, and JWT API.
- `budget-tracker-frontend/` — Angular standalone application.

## API overview

- `POST /api/auth/register` — create a workspace and its first administrator.
- `POST /api/auth/login` — sign in and receive access and refresh tokens.
- `/api/analytics/**` — monthly summaries, category spending, and trends.
- `/api/categories`, `/api/transactions`, `/api/budgets`, `/api/recurring-transactions` — workspace financial data.
- `/api/users/**` — profile and administrator team management.

Authenticated requests use `Authorization: Bearer <access-token>`. Tenant identity comes from the verified token, so records are scoped to the signed-in workspace.

## Common issues

- **Port 8080 is busy:** stop the other process using it before starting the backend. The frontend uses port 4200.
- **401 response:** the session is missing or expired; log in again.
- **403 response:** the signed-in user may not have permission for that action. Some operations require an administrator.
- **Empty lists or analytics:** the workspace may not have categories or transactions for the selected month.
- **Invitation email fails:** configure `MAIL_HOST`, `MAIL_PORT`, and any required `MAIL_*` credentials for an SMTP server.

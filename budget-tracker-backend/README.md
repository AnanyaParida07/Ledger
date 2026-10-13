# Budget Tracker backend

A Spring Boot backend for a multi-tenant SaaS expense tracker. It covers
registration, login, and automatic
per-tenant data isolation that doesn't require adding `tenant_id` checks to
every query by hand.

## How tenant isolation works

1. `POST /api/auth/register` creates a new `Tenant` row plus its first
   `User` (role `ADMIN`).
2. `POST /api/auth/login` returns a JWT whose claims include `tenantId`.
3. Every subsequent request sends that JWT in `Authorization: Bearer ...`.
4. `JwtAuthenticationFilter` decodes it and enables a Hibernate `@Filter`
   (defined once, on `BaseTenantEntity`) on the current session, scoped to
   that tenant's id.
5. From that point on, **any** query against an entity extending
   `BaseTenantEntity` — `categoryRepository.findAll()`, future
   `transactionRepository.findAll()`, anything — automatically gets
   `AND tenant_id = :tenantId` appended by Hibernate. Nobody has to
   remember to write that clause.

See the comments in `BaseTenantEntity` and `JwtAuthenticationFilter` for the
full explanation.

## Run locally on Windows

1. Start PostgreSQL and create the database once:
   ```sql
   CREATE DATABASE budgettracker;
   ```
2. Set the connection values in PowerShell if they differ from the local
   defaults (`postgres` / `an1234`):
   ```powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:5432/budgettracker'
   $env:DB_USERNAME = 'postgres'
   $env:DB_PASSWORD = '<your PostgreSQL password>'
   ```
3. From this directory, start the backend using IntelliJ's Maven tool window
   (`spring-boot:run`) or run `mvn spring-boot:run` if Maven is installed.
   The API listens on `http://localhost:8080`.
4. Start the Angular frontend separately from `budget-tracker-frontend` with
   `ng serve`; it listens on `http://localhost:4200` and calls the API on
   port 8080.

This project currently expects a database initialized with its base tables;
the versioned Flyway scripts add columns to that schema. Keep the existing
`budgettracker` database when restarting the application.

If the backend fails during startup, check its first `Caused by` message. A
connection-refused or password-authentication error means PostgreSQL is not
running or the `DB_*` values do not match the local PostgreSQL setup.

## Proving isolation works (curl walkthrough)

```bash
# Register tenant A
curl -s -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Acme Co","fullName":"Alice Admin","email":"[email protected]","password":"password123"}' \
  | tee /tmp/acme.json

TOKEN_A=$(jq -r .accessToken /tmp/acme.json)

# Register tenant B
curl -s -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Globex Inc","fullName":"Bob Admin","email":"[email protected]","password":"password123"}' \
  | tee /tmp/globex.json

TOKEN_B=$(jq -r .accessToken /tmp/globex.json)

# Tenant A creates a category
curl -s -X POST localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN_A" -H "Content-Type: application/json" \
  -d '{"name":"Acme Office Supplies"}'

# Tenant B creates a category
curl -s -X POST localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN_B" -H "Content-Type: application/json" \
  -d '{"name":"Globex Travel"}'

# Tenant A lists categories - should ONLY see "Acme Office Supplies"
curl -s localhost:8080/api/categories -H "Authorization: Bearer $TOKEN_A"

# Tenant B lists categories - should ONLY see "Globex Travel"
curl -s localhost:8080/api/categories -H "Authorization: Bearer $TOKEN_B"
```

If both calls return only their own tenant's row, isolation is working —
and notice `CategoryController`/`CategoryRepository` never once reference
`tenant_id`.

## Note on the JWT secret

Set `JWT_SECRET` to a private random key of at least 32 bytes outside local
development. The checked-in fallback is for local use only:

```powershell
$env:JWT_SECRET = '<private random key of at least 32 bytes>'
```

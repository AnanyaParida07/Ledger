# Ledger — frontend

Angular 18 standalone-component frontend for the multi-tenant budget
tracker. Talks to the Spring Boot backend in `budget-tracker-backend/`.

## Running it

```bash
npm install
npm start
```

Opens on `http://localhost:4200` and expects the backend running on
`http://localhost:8080` (see `src/environments/environment.ts`).

**Start the backend first** — the backend's `SecurityConfig` now includes a
CORS policy allowing `http://localhost:4200`, so the two just work together
once both are running. If you change the frontend's port, update
`corsConfigurationSource()` in the backend to match.

## What's here

- `core/services/auth.service.ts` — holds the session as Angular signals,
  persists tokens to `localStorage`, exposes `login()`/`register()`/`logout()`.
- `core/interceptors/auth.interceptor.ts` — a functional `HttpInterceptorFn`
  that attaches `Authorization: Bearer <token>` to every request except
  `/auth/login` and `/auth/register`, and logs out on a 401.
- `core/guards/auth.guard.ts` — a functional `CanActivateFn` that redirects
  to `/login` (preserving `returnUrl`) when there's no session.
- `features/auth/` — login and register pages, sharing a two-panel
  `AuthLayoutComponent`.
- `features/dashboard/` — a minimal categories list + add form. This is
  what actually proves the backend's tenant isolation works: it calls
  `GET /api/categories` with zero tenant filtering on the frontend side,
  and only ever sees the logged-in tenant's rows.

## Try it end to end

1. Run the backend, then this frontend.
2. Go to `http://localhost:4200/register`, create a workspace ("Acme Co").
3. You land on `/dashboard` — add a category, see it appear.
4. Log out, register a second workspace ("Globex Inc") with a different
   email, add a different category.
5. Notice each workspace only ever sees its own categories — that's the
   backend's Hibernate tenant filter doing its job, invisibly, from the
   frontend's point of view.

## Next steps

- Wire up `Transaction` and `Budget` pages once those backend endpoints
  exist (Phase 2 of the backend plan).
- Add a token-refresh flow using the `refreshToken` already returned by
  login/register — right now it's stored but unused; the access token just
  expires after 15 minutes and the interceptor logs the person out on the
  next 401.
- Swap `localStorage` for an httpOnly cookie issued by the backend if you
  want to harden this against XSS before it's anything more than a
  portfolio piece.

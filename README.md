# ProTech One — Pure Java/HTML/CSS Edition (no client-side JavaScript)

This is the server-rendered version of ProTech One Phase 1: **Java (Spring Boot + Spring MVC + Thymeleaf) + HTML + CSS only** — every page is rendered on the server and sent to the browser as plain HTML. There is no `fetch()`, no React/Vue, no jQuery, no Bootstrap JS, no Chart.js, no DataTables — zero `<script>` tags anywhere in the templates. Interactivity that would normally need JavaScript (dropdown menus, the collapsible sidebar, dark mode, RTL) is implemented with plain HTML/CSS techniques instead (see "How the JS-free bits work" below).

It shares the same database schema, entities, and most of the business-logic services as the earlier JWT/REST + JS edition (`protech-one-api` / `protech-one-frontend`) — only the web layer changed, from a JSON REST API + SPA to classic server-side MVC.

## Tech stack

- **Java 21, Spring Boot 3.3**
- **Spring MVC + Thymeleaf** for server-rendered HTML (no REST controllers, no JSON)
- **Spring Security** with classic session-based form login (no JWT) — CSRF protection is on by default and every form carries the token automatically via Thymeleaf's Spring integration
- **Spring Data JPA / Hibernate**, **Flyway**, **PostgreSQL** (MySQL driver also included)
- **Plain CSS3** — the same design system (colors, cards, tables, status badges) as the JS edition, reused as-is since it never depended on JavaScript

## Folder structure

```
protech-one-webapp/
├── pom.xml
└── src/main/
    ├── java/com/protechone/
    │   ├── controller/     Spring MVC controllers (return view names, not JSON)
    │   ├── service/        business logic (mostly unchanged from the REST edition)
    │   ├── entity/          JPA entities
    │   ├── repository/      Spring Data repositories
    │   ├── dto/              request/response records, reused as Thymeleaf view models
    │   ├── security/        SecurityConfig (form login), UserDetailsServiceImpl,
    │   │                      AuthenticationEventListener (login history + lockout)
    │   └── exception/       GlobalControllerAdvice (renders HTML error pages)
    └── resources/
        ├── application.yml
        ├── db/migration/     same Flyway schema + seed data as the REST edition
        ├── templates/         Thymeleaf HTML templates, one per screen
        │   ├── fragments/layout.html   shared sidebar/topbar/alerts fragments
        │   ├── auth/, dashboard/, crm/, purchasing/, inventory/, sales/,
        │   │   accounting/, reports/, settings/, search/, error/
        └── static/css/        plain CSS (no JS)
```

## Default login

`admin@protechone.com` / `Admin@123` (seeded by `V2__seed_data.sql`, same as the other edition). Change it after first login, or click "Create your company workspace" on the login page to register a fresh company.

## Running it

Prerequisites: Java 21, Maven 3.9+, PostgreSQL 14+ (or MySQL 8+).

```sql
CREATE DATABASE protech_one;
CREATE USER protech WITH PASSWORD 'protech';
GRANT ALL PRIVILEGES ON DATABASE protech_one TO protech;
```

```bash
mvn spring-boot:run
```

Open `http://localhost:8080/` — that's it, one app serves everything (HTML, CSS, and the Java backend all together, no separate frontend server needed). Flyway creates the schema and seed data automatically on first boot.

> Same disclaimer as the other edition: this was authored in a sandbox without Maven/internet access, so it hasn't been compiled here. It follows current Spring Boot 3.3 / Thymeleaf / Spring Security 6 conventions and should build with `mvn clean install` on a normal machine — please report back if anything doesn't compile.

## How the JS-free bits work

Every place a typical admin dashboard reaches for JavaScript, this build uses a plain HTML/CSS equivalent instead:

- **Dropdown menus** (notification bell, user menu) use the native `<details>`/`<summary>` HTML element — click to open/close, no JS.
- **Collapsible sidebar / mobile drawer** uses the "checkbox hack": two hidden `<input type="checkbox">` elements sit right before the page shell, and `<label for="...">` buttons toggle them; CSS `:checked ~ .sibling` selectors show/hide the sidebar (see `static/css/responsive.css`).
- **Dark mode / RTL toggle** are plain links to `/prefs/theme` and `/prefs/dir`, which set a cookie server-side and redirect back — no `localStorage`, no JS.
- **Global search** is a normal `<form method="get">` that submits to `/search?q=...` and renders a results page.
- **Tables** are plain server-rendered `<table>` markup (no DataTables) with pagination as plain page links.
- **Charts** ("Sales by Month", "Top-Selling Products") are CSS-only horizontal bar charts — the controller pre-computes each bar's width as a percentage in Java and renders it as an inline `style="width:NN%"`, no Chart.js.
- **Modals** are replaced with dedicated pages (e.g. "Add Customer" is its own `/customers/new` page, not a JS-driven modal on the list page).
- **Confirmation dialogs** ("Delete this customer?") are dedicated confirm pages with a real POST form, instead of a JS `confirm()`.
- **Flash messages** (success/error banners) use Spring's `RedirectAttributes` flash-scoped model attributes, rendered as a plain `<div class="alert">` — no toast library, no auto-dismiss timer.
- **CSV export** on the Reports page is a real server-rendered download (`GET /reports/export.csv`, `Content-Disposition: attachment`) rather than a client-side Blob download.
- **Printing** relies on the browser's native Ctrl/Cmd+P — `@media print` rules in `responsive.css` hide the sidebar/topbar/action buttons automatically.
- **Sales/Purchase invoice line items**: since there's no JS to dynamically add/remove rows, the "new invoice" form renders 10 blank line-item rows up front; Spring's indexed data binding (`items[0].productId`, `items[1].productId`, ...) picks up whichever rows you filled in, and blank rows are dropped before saving.

## What's reused vs. rewritten from the REST/JS edition

**Reused as-is:** entities, repositories, the Flyway schema/seed data, and most services (Customer, Supplier, Inventory, Sales/Purchase Invoice, Expense, Dashboard, Report, User, Company, Notification). `CurrentUser` (multi-tenant scoping) and `UserDetailsServiceImpl` needed no changes at all — they work identically under session auth.

**Rewritten:** the entire web layer. JWT (`JwtService`, `JwtAuthFilter`) was replaced with Spring Security's declarative `formLogin` + an `AuthenticationEventListener` that reimplements account lockout and login-history logging via Spring Security's authentication events instead of manual code in a login endpoint. All 14 REST `@RestController` classes were replaced with 17 MVC `@Controller` classes that return Thymeleaf view names. `AuthService` was trimmed to just registration/password-reset (login itself is now handled declaratively by Spring Security, not application code).

## Known limitations (same as the REST/JS edition, plus one)

- Sales/Purchasing are a single Invoice document, not the full Quotation→Order→Delivery→Invoice chain.
- Email delivery (password reset, verification) is stubbed — tokens are generated but nothing sends the email yet.
- MFA fields exist on `users` but enrollment isn't implemented.
- PDF invoice generation isn't implemented; CSV export works today.
- **New in this edition:** because there's no live client-side recalculation, the invoice line-item totals (subtotal/discount/tax/grand total) only appear after you save the invoice and land on its detail page — there's no running total preview while you're filling in the form.

# Employee Database

A full-stack Employee Management System built with:

- **Backend** — Spring Boot 3.2 · Spring Security (JWT) · JPA/H2 · springdoc Swagger · iText (PDF) · Apache POI (Excel)
- **Frontend** — Angular 19 · Bootstrap 5 · Reactive Forms

---

## Features

| Feature | Details |
|---|---|
| Authentication | Login & Sign-up with JWT tokens |
| Role-based access | **ADMIN** and **USER** roles |
| Employee CRUD | Create, Read (single + all), Update, Delete |
| Search & Filter | Search by name/email/username; filter by department |
| Pagination | Server-side pagination with page controls |
| Validation | Client-side (Angular) + Server-side (Bean Validation) with regex rules |
| Export to PDF | Admin only — iText 8 |
| Export to Excel | Admin only — Apache POI |
| API Documentation | Swagger UI at `/swagger-ui.html` |

---

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java | 17+ | [adoptium.net](https://adoptium.net) |
| Maven | 3.8+ | `brew install maven` or [maven.apache.org](https://maven.apache.org) |
| Node.js | 18+ | [nodejs.org](https://nodejs.org) |
| npm | 9+ | Comes with Node.js |

---

## Running on Localhost

### 1. Clone / open the project

```bash
cd "emp"
```

---

### 2. Start the Backend (Spring Boot API)

```bash
cd backend
mvn spring-boot:run
```

- Starts on **http://localhost:8080**
- Uses an **in-memory H2 database** (no setup needed — resets on restart)
- Seed accounts created automatically on first run:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `user1` | `user123` | USER |

**Useful backend URLs:**

| URL | Description |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API docs |
| http://localhost:8080/h2-console | Database browser (JDBC URL: `jdbc:h2:mem:employeedb`) |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

---

### 3. Start the Frontend (Angular)

Open a **new terminal tab**, then:

```bash
cd frontend
npm install        # only needed the first time
npm start
```

- Starts on **http://localhost:4200**
- Hot-reload is enabled — changes reflect instantly

---

### 4. Open the App

Go to **http://localhost:4200** in your browser.

- Log in as `admin` / `admin123` to access all features (delete, export PDF/Excel)
- Log in as `user1` / `user123` to browse and create/edit employees

---

## Project Structure

```
emp/
├── backend/                          # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/employeedb/
│       ├── config/                   # OpenAPI config, data seed
│       ├── dto/                      # Request/response DTOs
│       ├── model/                    # JPA entities (Employee, AppUser, Role)
│       ├── repo/                     # Spring Data JPA repositories
│       ├── security/                 # JWT filter, UserDetails, SecurityConfig
│       ├── service/                  # Business logic + PDF/Excel export
│       └── web/                     # REST controllers + exception handler
│
└── frontend/                         # Angular 19 app
    └── src/app/
        ├── core/                     # Auth service, Employee service, interceptor, guard
        └── pages/
            ├── login/
            ├── register/
            └── employees/            # CRUD table, search/filter/pagination, modal form
```

---

## API Quick Reference

All employee endpoints require a `Bearer <token>` header.

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Sign up |
| POST | `/api/auth/login` | Public | Log in, get token |
| GET | `/api/employees?q=&department=&page=&size=` | USER/ADMIN | List with search + filter + pagination |
| GET | `/api/employees/{id}` | USER/ADMIN | Get one employee |
| POST | `/api/employees` | USER/ADMIN | Create employee |
| PUT | `/api/employees/{id}` | USER/ADMIN | Update employee |
| DELETE | `/api/employees/{id}` | ADMIN | Delete employee |
| GET | `/api/employees/export/pdf` | ADMIN | Download PDF |
| GET | `/api/employees/export/excel` | ADMIN | Download Excel |

---

## Validation Rules

| Field | Rule |
|---|---|
| Username | `^[a-zA-Z0-9_]{3,50}$` |
| Email | Valid email format |
| Age | 18 – 100 |
| Mobile | Exactly 10 digits |
| Password | Min 6 characters |

Rules are enforced on **both** client (Angular) and server (Spring Boot).

---

## Stopping the Servers

- **Backend**: `Ctrl + C` in the terminal running `mvn spring-boot:run`
- **Frontend**: `Ctrl + C` in the terminal running `npm start`

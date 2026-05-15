# 🗃️ Employee Database System

A full-stack **Employee Management System** with role-based access control, built as a college project.

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2 · Spring Security · JWT · JPA · H2 (local file) |
| Frontend | Angular 19 · Bootstrap 5 · Reactive Forms |
| API Docs | Swagger / OpenAPI (springdoc) |
| Export | iText 8 (PDF) · Apache POI (Excel) |

---

## 📋 Features

- **Login & Sign-up** with JWT authentication
- **Role-Based Access Control** — Admin and User roles
- **Employee CRUD** — Create, Read (single & all), Update, Delete
- **Search** by name, email, or username
- **Filter** by department
- **Pagination** — server-side with page navigation
- **Client-side + Server-side Validation** with regex rules
- **Export to PDF** (Admin only) — powered by iText
- **Export to Excel** (Admin only) — powered by Apache POI
- **Swagger UI** — interactive API documentation

---

## 🖥️ Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** + **JWT** (jjwt 0.12)
- **Spring Data JPA** + **Hibernate**
- **H2** local file database
- **springdoc OpenAPI** (Swagger UI)
- **iText 8** (PDF export)
- **Apache POI 5** (Excel export)
- **Bean Validation** (server-side regex rules)

### Frontend
- **Angular 19** (standalone components)
- **Bootstrap 5** (via SCSS)
- **Angular Reactive Forms** (client-side validation)
- **RxJS** (HTTP + interceptors)

---

## 🚀 Getting Started (Windows)

### Prerequisites — Install these first

#### 1. Java 17
- Download from **https://adoptium.net** → Temurin 17 LTS → Windows `.msi`
- During install: tick ✅ **Set JAVA_HOME** and ✅ **Add to PATH**
- Verify:
  ```cmd
  java -version
  ```

#### 2. Maven
- Download from **https://maven.apache.org/download.cgi** → Binary zip
- Extract to `C:\Program Files\Maven\`
- Add `C:\Program Files\Maven\apache-maven-3.9.x\bin` to **System PATH**
- Verify:
  ```cmd
  mvn -version
  ```

#### 3. Node.js (LTS)
- Download from **https://nodejs.org** → LTS → Windows `.msi`
- Verify:
  ```cmd
  node -v
  npm -v
  ```

#### 4. Database (local file)
- No external database is required.
- The backend uses an **H2** file stored under your user home directory at `~/.employee-db/employeedb` (`%USERPROFILE%\.employee-db\employeedb` on Windows).

---

### Step 1 — Clone the Project

```cmd
git clone https://github.com/YOUR_USERNAME/employee-db.git
cd employee-db
```

> Or download the ZIP from GitHub → Code → Download ZIP → extract it.

---

### Step 2 — Start the Backend

Open a Command Prompt in the `backend` folder:

```cmd
cd employee-db\backend
mvn spring-boot:run
```

Wait for:
```
Started EmployeeDbApplication in X seconds
```

✅ API running at **http://localhost:8080**

> Tables are **auto-created** by Hibernate on first run.
> Seed accounts (`admin`, `user1`) are inserted automatically.

---

### Step 3 — Start the Frontend

Open a **second** Command Prompt in the `frontend` folder:

```cmd
cd employee-db\frontend
npm install
npm start
```

Wait for:
```
Local:   http://localhost:4200/
```

✅ App running at **http://localhost:4200**

---

## 🔐 Demo Accounts

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | All features including delete & export |
| `user1` | `user123` | USER | View, create, edit only |

> New accounts created via Sign-up are always assigned the **USER** role.

---

## 🌐 URLs

| URL | Description |
|---|---|
| http://localhost:4200 | Frontend (Angular app) |
| http://localhost:8080/swagger-ui.html | Swagger API docs |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

---

## 📁 Project Structure

```
employee-db/
│
├── backend/                              # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/employeedb/
│       ├── config/
│       │   ├── DataInitializer.java      # Seeds admin & user1 on startup
│       │   └── OpenApiConfig.java        # Swagger / JWT security scheme
│       ├── dto/                          # LoginRequest, RegisterRequest, AuthResponse
│       ├── model/                        # Employee, AppUser, Role (enum)
│       ├── repo/                         # Spring Data JPA repositories
│       ├── security/
│       │   ├── JwtService.java           # Token generate / validate
│       │   ├── JwtAuthFilter.java        # Bearer token filter
│       │   ├── SecurityConfig.java       # CORS, role rules, stateless session
│       │   └── AppUserDetails.java       # UserDetails adapter
│       ├── service/
│       │   ├── AuthService.java          # Register & authenticate
│       │   ├── EmployeeService.java      # CRUD + search/filter logic
│       │   ├── spec/EmployeeSpecs.java   # JPA Specifications (dynamic query)
│       │   └── export/
│       │       ├── EmployeePdfExportService.java
│       │       └── EmployeeExcelExportService.java
│       └── web/
│           ├── AuthController.java       # /api/auth/**
│           ├── EmployeeController.java   # /api/employees/**
│           └── GlobalExceptionHandler.java
│
└── frontend/                             # Angular 19 app
    └── src/app/
        ├── core/
        │   ├── auth.service.ts           # Login, register, session (signal-based)
        │   ├── auth.guard.ts             # Route guard
        │   ├── auth.interceptor.ts       # Attaches Bearer token to requests
        │   ├── employee.service.ts       # CRUD + export API calls
        │   └── models.ts                 # TypeScript interfaces
        └── pages/
            ├── login/                    # Login form
            ├── register/                 # Sign-up form
            └── employees/               # Main table, modal form, pagination
```

---

## 📡 API Reference

All endpoints under `/api/employees/**` require:
```
Authorization: Bearer <your_jwt_token>
```

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create account (USER role) |
| POST | `/api/auth/login` | Public | Login, receive JWT token |
| GET | `/api/employees` | USER / ADMIN | List employees (search, filter, page) |
| GET | `/api/employees/{id}` | USER / ADMIN | Get single employee |
| POST | `/api/employees` | USER / ADMIN | Create employee |
| PUT | `/api/employees/{id}` | USER / ADMIN | Update employee |
| DELETE | `/api/employees/{id}` | ADMIN only | Delete employee |
| GET | `/api/employees/export/pdf` | ADMIN only | Download PDF report |
| GET | `/api/employees/export/excel` | ADMIN only | Download Excel report |

### Query Parameters (GET /api/employees)

| Param | Type | Description |
|---|---|---|
| `q` | string | Search username, email, first/last name |
| `department` | string | Filter by exact department name |
| `page` | int | Page number (0-based, default 0) |
| `size` | int | Page size (default 10) |
| `sort` | string | e.g. `lastName,asc` or `id,desc` |

---

## ✅ Validation Rules

Applied on **both** Angular (client) and Spring Boot (server):

| Field | Rule |
|---|---|
| Username | `^[a-zA-Z0-9_]{3,50}$` — letters, digits, underscore |
| Email | Valid email format |
| Age | Between 18 and 100 |
| Mobile | Exactly 10 digits |
| Password | Minimum 6 characters |

---

## 🗄️ Database

| Setting | Value |
|---|---|
| Engine | H2 (local file) |
| File | `~/.employee-db/employeedb` (`%USERPROFILE%\.employee-db\employeedb` on Windows) |
| Username | `sa` |
| Password | Set via `EMPLOYEE_DB_PASSWORD` |

> Set `EMPLOYEE_DB_PASSWORD` (for example, `localdev`) before starting the backend.

Connection string (in `application.properties`):
```
jdbc:h2:file:${user.home}/.employee-db/employeedb
```

---

## ⚙️ Configuration

`backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:h2:file:${user.home}/.employee-db/employeedb
spring.datasource.username=sa
spring.datasource.password=${EMPLOYEE_DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
jwt.secret=${JWT_SECRET:changeme-use-a-long-secret-key-at-least-256-bits-for-hs256-xxxxx}
jwt.expiration-ms=86400000
```

> Set `JWT_SECRET` to a strong value for any non-local environment.

> The backend will fail to start if `EMPLOYEE_DB_PASSWORD` is not set.
> Windows (Command Prompt): `set EMPLOYEE_DB_PASSWORD=localdev`
> macOS/Linux: `export EMPLOYEE_DB_PASSWORD=localdev`

---

## 🛑 Stopping the Servers

Press **Ctrl + C** in each Command Prompt window.

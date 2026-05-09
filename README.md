# Employee Database

A full-stack Employee Management System built with:

- **Backend** — Spring Boot 3.2 · Spring Security (JWT) · JPA/MySQL · springdoc Swagger · iText (PDF) · Apache POI (Excel)
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

## Running on Windows (Fresh Install)

Follow these steps **in order** on a brand new Windows machine.

---

### Step 1 — Install Java 17

1. Go to **https://adoptium.net**
2. Download **Temurin 17 (LTS)** → Windows → `.msi` installer
3. Run the installer — tick **"Set JAVA_HOME"** and **"Add to PATH"** during setup
4. Verify in a new Command Prompt:
   ```cmd
   java -version
   ```
   You should see `openjdk version "17.x.x"`

---

### Step 2 — Install Maven

1. Go to **https://maven.apache.org/download.cgi**
2. Download the **Binary zip archive** (e.g. `apache-maven-3.9.x-bin.zip`)
3. Extract it to `C:\Program Files\Maven\`
4. Add Maven to PATH:
   - Search **"Environment Variables"** in Windows search
   - Under **System Variables** → find `Path` → click Edit → New
   - Add: `C:\Program Files\Maven\apache-maven-3.9.x\bin`
5. Verify in a new Command Prompt:
   ```cmd
   mvn -version
   ```

---

### Step 3 — Install Node.js

1. Go to **https://nodejs.org**
2. Download the **LTS version** (e.g. 20.x) → Windows Installer `.msi`
3. Run the installer (keep all defaults)
4. Verify in a new Command Prompt:
   ```cmd
   node -v
   npm -v
   ```

---

### Step 4 — Install MySQL

1. Go to **https://dev.mysql.com/downloads/installer/**
2. Download **MySQL Installer for Windows** (the full `mysql-installer-community` version)
3. Run the installer → choose **"Developer Default"** setup type → click Execute (installs MySQL Server + tools)
4. During configuration:
   - Authentication Method → **Use Legacy Authentication Method** (easier for local dev)
   - Set a **root password** (remember it)
   - Leave port as **3306**
5. Finish installation
6. Verify — open **MySQL Command Line Client** from Start Menu and log in with root password

---

### Step 5 — Create the Database

Open **MySQL Command Line Client** (or MySQL Workbench) and run:

```sql
CREATE DATABASE IF NOT EXISTS employeedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'empuser'@'localhost' IDENTIFIED BY 'emppass123';
GRANT ALL PRIVILEGES ON employeedb.* TO 'empuser'@'localhost';
FLUSH PRIVILEGES;
```

---

### Step 6 — Download the Project

Option A — with Git:
```cmd
git clone https://github.com/YOUR_USERNAME/employee-db.git
cd employee-db
```

Option B — without Git:
- Go to the GitHub repo → click **Code → Download ZIP**
- Extract the ZIP somewhere (e.g. `C:\Projects\employee-db`)

---

### Step 7 — Start the Backend

Open a **Command Prompt** in the `backend` folder:

```cmd
cd C:\Projects\employee-db\backend
mvn spring-boot:run
```

Wait until you see:
```
Started EmployeeDbApplication in X seconds
```

The API is now running at **http://localhost:8080**

---

### Step 8 — Start the Frontend

Open a **second Command Prompt** in the `frontend` folder:

```cmd
cd C:\Projects\employee-db\frontend
npm install
npm start
```

Wait until you see:
```
Local: http://localhost:4200/
```

---

### Step 9 — Open the App

Go to **http://localhost:4200** in your browser.

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Full access (delete, export PDF/Excel) |
| `user1` | `user123` | View, create, edit only |

---

## Useful URLs

| URL | Description |
|---|---|
| http://localhost:4200 | Angular frontend |
| http://localhost:8080/swagger-ui.html | API documentation (Swagger) |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

---

## Stopping the Servers

- Press **Ctrl + C** in each Command Prompt window

---

## Project Structure

```
employee-db/
├── backend/                          # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/employeedb/
│       ├── config/                   # OpenAPI config, data seed
│       ├── dto/                      # Request/response DTOs
│       ├── model/                    # JPA entities (Employee, AppUser, Role)
│       ├── repo/                     # Spring Data JPA repositories
│       ├── security/                 # JWT filter, UserDetails, SecurityConfig
│       ├── service/                  # Business logic + PDF/Excel export
│       └── web/                      # REST controllers + exception handler
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

All employee endpoints require `Authorization: Bearer <token>` header.

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

## Database

- **MySQL** running on `localhost:3306`
- Database: `employeedb`
- User: `empuser` / Password: `emppass123`
- Tables are **auto-created** by Hibernate on first run (`spring.jpa.hibernate.ddl-auto=update`)
- Seed data (admin + user1 accounts, 1 sample employee) is inserted automatically on startup

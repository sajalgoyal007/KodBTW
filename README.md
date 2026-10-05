# KodBTW

**KodBTW** is a Java-based unified coding profile and analytics platform designed for students and developers to connect multiple coding profiles (LeetCode, CodeChef, Codeforces, GeeksforGeeks, HackerRank) and view their statistics, progress, and rankings in one cohesive dashboard.

---

## 🛠️ Technology Stack

- **Backend:** Java 21 LTS, Spring Boot 3.3.x, Spring Data JPA / Hibernate, Spring Security, BCrypt, JJWT, Maven
- **Database & Migrations:** MySQL 8.x with Flyway versioned migrations
- **Frontend (Upcoming):** React, TypeScript, Vite, Vanilla CSS Design System (Stitch Dark Theme + Orange Accents)

---

## 📁 Repository Structure

```
KodBTW/
├── backend/                  # Spring Boot REST API
│   ├── src/main/java/com/kodbtw/
│   │   ├── config/           # Security and CORS configurations
│   │   ├── controller/       # AuthController, HealthController
│   │   ├── dto/              # Request / Response transfer objects
│   │   ├── entity/           # User and JPA entities
│   │   ├── exception/        # Global exception handler & domain errors
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JwtService, JwtAuthenticationFilter
│   │   └── service/          # AuthService, UserService
│   ├── src/main/resources/
│   │   ├── application.yml   # Environment-driven application config
│   │   └── db/migration/     # Flyway SQL migrations (V1, V2)
│   └── src/test/             # Comprehensive JUnit 5 & MockMvc tests
├── frontend/                 # React + TypeScript UI (Vite)
└── docs/                     # Architecture & roadmap documentation
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 21 LTS**
- **MySQL 8.0+**
- **Node.js 18+** & **npm**

### Database Setup
Ensure MySQL is running locally on port `3306`:
```sql
CREATE DATABASE IF NOT EXISTS kodbtw;
```

### Running the Backend
From the `backend/` directory:

```bash
# Set database credentials (or configure environment variables)
export DB_USERNAME=root
export DB_PASSWORD=root
export DB_URL="jdbc:mysql://localhost:3306/kodbtw?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

# Run tests
./mvnw clean test

# Run application
./mvnw spring-boot:run
```

On Windows PowerShell:
```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
$env:DB_URL = "jdbc:mysql://localhost:3306/kodbtw?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

---

## 🧪 Current API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/health` | Public | Service health probe |
| `POST` | `/api/auth/register` | Public | Register new user account |
| `POST` | `/api/auth/login` | Public | Authenticate user and receive JWT |
| `GET` | `/api/auth/me` | Protected (`Bearer <token>`) | Retrieve current authenticated user profile |

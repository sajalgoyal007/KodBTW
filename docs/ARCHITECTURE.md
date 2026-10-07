# KodBTW — Project Architecture & Roadmap

## 1. Overview
KodBTW is a Java-based unified coding profile and analytics platform enabling students to connect multiple coding profiles (LeetCode, CodeChef, Codeforces, GeeksforGeeks, HackerRank) and view their statistics, progress, and rankings in one cohesive dashboard.

## 2. Core Tech Stack
- **Backend:** Java 21 LTS, Spring Boot 3.3.x, Spring Data JPA / Hibernate, Spring Security, BCrypt, JWT, Maven
- **Database & Migrations:** MySQL 8.x with Flyway migrations
- **Frontend:** React, TypeScript, Vite, Vanilla CSS design system (Dark theme with vibrant orange accents based on Google Stitch designs)

## 3. Monorepo Organization
- `backend/`: Spring Boot REST API application
- `frontend/`: React + TypeScript frontend application
- `docs/`: Architectural specifications, schema diagrams, and project documentation

## 4. Phased Implementation Roadmap
- **Phase 1:** Monorepo scaffolding and directory structure (Current)
- **Phase 2:** Spring Boot + MySQL + Flyway database configuration
- **Phase 3:** User entity, authentication, BCrypt, and JWT implementation
- **Phase 4:** User profile management
- **Phase 5:** PlatformAccount and seeded platform statistics
- **Phase 6:** Dashboard analytics APIs
- **Phase 7:** Frontend foundation and design system (Dark + Orange)
- **Phase 8:** Incremental screen implementation (12 screens)
- **Phase 9:** Frontend-backend integration
- **Phase 10:** Public developer profiles and profile sharing, including unique public usernames, an unauthenticated sanitized profile API, profile sharing UI, and leaderboard links to public profiles

# Finance Tracker API (Backend)

## Overview

The Finance Tracker API is a Spring Boot application that provides a secure and scalable RESTful interface for managing personal finance data. It supports CRUD operations for transactions and categories, user profile management, and integrates with Auth0 for authentication and authorization.

## App Architecture

<img width="1119" alt="Screenshot 2025-05-30 at 7 37 54 PM" src="https://github.com/user-attachments/assets/968cf606-5277-4aab-9aa4-3dfab173af16" />


## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
  - [Transactions](#transactions)
  - [Categories](#categories)
  - [User Profile](#user-profile)
- [Data Model](#data-model)
- [Security](#security)
- [Database Migrations](#database-migrations)
- [Caching](#caching)
- [Exception Handling](#exception-handling)
- [Actuator](#actuator)
- [Future Enhancements](#future-enhancements)

## Features

- CRUD operations for transactions and categories
- Pagination and filtering (by type, date range, category)
- User profile management (fetch, update, delete)
- Secure endpoints with JWT (Auth0) and scope-based authorization
- Method-level security and ownership checks
- Global exception handling with RFC-7807 Problem Details
- Flyway-based database migrations and seed data
- Spring Cache for categories
- Actuator endpoints for health and metrics

## Technology Stack

- Java 21
- Spring Boot 3.4.x
- Spring Security OAuth2 Resource Server (JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL (via Docker Compose)
- Flyway for migrations
- Lombok for boilerplate reduction
- Spring Cache
- Spring Boot Actuator
- Maven for dependency management

## Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- Docker & Docker Compose

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-org/finance-tracker-api.git
   cd finance-tracker-api/backend
   ```

2. **Configure environment variables**

   ```bash
   export POSTGRES_USER=finance_user
   export POSTGRES_PASSWORD=strong_password
   export POSTGRES_DB=finance_db
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/${POSTGRES_DB}
   export SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
   export SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
   ```

3. **Start the PostgreSQL database**

   ```bash
   docker compose up -d
   ```

4. **Build and run the application**

   ```bash
   mvn clean install
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **Access the API**
   - Base URL: `http://localhost:8080/api`
   - H2 Console: (not enabled by default)

## Configuration

Configuration files are located in `src/main/resources`:

- `application.properties`: base settings (application name, port, JPA SQL logging)
- `application-dev.yml`: development profile (Actuator exposure, Flyway, security settings)

### Environment Variables

| Variable                     | Description               |
| ---------------------------- | ------------------------- |
| `POSTGRES_USER`              | PostgreSQL username       |
| `POSTGRES_PASSWORD`          | PostgreSQL password       |
| `POSTGRES_DB`                | PostgreSQL database name  |
| `SPRING_DATASOURCE_URL`      | JDBC URL for the database |
| `SPRING_DATASOURCE_USERNAME` | Database username         |
| `SPRING_DATASOURCE_PASSWORD` | Database password         |

Authentication settings (issuer, audience) are currently hard-coded in `SecurityConfig` but can be externalized if needed.

## API Endpoints

### Transactions

- `GET /api/transactions`  
  Returns a paginated list of all transactions belonging to the authenticated user.
- `GET /api/transactions/{id}`  
  Fetch a single transaction by its ID.
- `GET /api/transactions/type/{type}`  
  Fetch transactions by type (`INCOME`, `EXPENSE`, `TRANSFER`).
- `GET /api/transactions/date-range?startDate={ISO_DATE_TIME}&endDate={ISO_DATE_TIME}`  
  Fetch transactions between two dates (inclusive).
- `GET /api/transactions/category/{categoryId}`  
  Fetch transactions for a specific category.
- `POST /api/transactions`  
  Create a new transaction.  
  Request body: `TransactionDTO`.
- `PUT /api/transactions/{id}`  
  Update an existing transaction.  
  Request body: `TransactionDTO`.
- `DELETE /api/transactions/{id}`  
  Delete a transaction by ID.

### Categories

- `GET /api/categories`  
  Returns a paginated list of all categories.
- `GET /api/categories/{id}`  
  Fetch a category by its ID.
- `GET /api/categories/name/{name}`  
  Fetch a category by its name.
- `POST /api/categories`  
  Create a new category.  
  Request body: `CategoryDTO`.
- `PUT /api/categories/{id}`  
  Update an existing category.  
  Request body: `CategoryDTO`.
- `DELETE /api/categories/{id}`  
  Delete a category by ID.

### User Profile

- `GET /api/users/me`  
  Fetch the authenticated user's profile.
- `PUT /api/users/me`  
  Update the authenticated user's profile.  
  Request body: `UserDTO`.
- `DELETE /api/users/me`  
  Delete the authenticated user's account.

## Data Model

### Entities

- **AppUser**  
  Stores users with fields: `id`, `auth0Sub`, `name`, `email`.
- **Category**  
  Stores spending categories: `id`, `name`, `description`.
- **Transaction**  
  Stores transactions: `id`, `description`, `amount`, `date`, `type`, `notes`, `category`, `user`.

### DTOs

- **UserDTO**: `id`, `auth0Sub`, `name`, `email`.
- **CategoryDTO**: `id`, `name`, `description`.
- **TransactionDTO**: `id`, `description`, `amount`, `date`, `type`, `categoryId`, `categoryName`, `notes`.

## Security

- **Auth0 Integration**: Validates JWT access tokens issued by Auth0.
- **Scopes**: Requires scope `fin:app` to access API endpoints.
- **Method Security**: Uses `@PreAuthorize` annotations and a custom `OwnershipEvaluator` bean to enforce resource ownership.
- **CORS**: Configured to allow requests from `https://app.finsight.com`.
- **HTTP Security Headers**: Content Security Policy, HSTS, Referrer Policy.

## Database Migrations

Flyway migrations are stored in `src/main/resources/db/migration`:

- `V1__init.sql`: Creates tables (`users`, `categories`, `transactions`) and indexes.
- `R__seed_categories.sql`: Inserts initial category data.

## Caching

Categories endpoints are cached using Spring Cache (cache name: `categories`). Cache is evicted on create/update/delete operations.

## Exception Handling

A global exception handler (`GlobalExceptionHandler`) returns standardized RFC-7807 Problem Details with fields:

- `type`: URI identifying error type
- `title`: HTTP status reason phrase
- `status`: HTTP status code
- `detail`: error message
- `timestamp`: error occurrence time
- `errors` (for validation failures): field-level error details

## Actuator

- `GET /actuator/health`: Public health check.
- `GET /actuator/metrics`: Requires `fin:app` scope.

## Future Enhancements

- Externalize Auth0 configuration via environment variables
- Rate limiting using Bucket4j (dependency included)
- Role-based access control and custom permissions
- Advanced reporting and analytics endpoints
- Frontend application and mobile clients
- AI-driven spending analysis and recommendations

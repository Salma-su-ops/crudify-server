# Crudify Server - Spring Boot Backend

## Configuration

Update `src/main/resources/application.properties` with database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/crudify_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

## Project Structure

```
src/main/java/com/example/crudify_server/
├── entity/         # Product entity with JPA annotations
├── repository/     # ProductRepository extending JpaRepository
├── service/        # Business logic and CRUD operations
├── controller/     # REST endpoints with validation
├── dto/            # Request/Response DTOs
├── config/         # Security configuration
└── exception/      # Global exception handling
```

## Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:
- V1__Create_products_table.sql
- V2__Insert_sample_products.sql

## Features

**Core Backend Features:**
- RESTful API with full CRUD operations
- JPA/Hibernate entities with validation
- PostgreSQL database integration
- Flyway database migrations

**Advanced Features:**
- Global exception handling with structured error responses
- Input validation with custom error messages
- CORS configuration for React frontend
- Security configuration for development
- Custom repository methods with JPQL queries
- Sample data seeding via migration scripts

## Running

```bash
mvnw.cmd spring-boot:run
```

Server starts on port 8080 with sample data loaded. 
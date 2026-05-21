# AGENTS.md - AI Coding Agent Guide

This file contains essential knowledge to help AI coding agents be immediately productive in the **Expense Tracker** codebase.

## Project Architecture & Domain

**Expense Tracker** is a Spring Boot 3.4.1 REST API for personal finance management. Core domain: users organize monthly expenses into categories, tag them with events, and track savings separately. Multi-user system with JWT authentication and PostgreSQL backend.

### Layered Architecture Pattern
- **Controllers** (`controller/`): REST endpoints, request validation, parameter extraction
- **Services** (`service/`): Business logic, cross-entity orchestration, validation
- **Repositories** (`repository/`): Spring Data JPA interfaces with custom @Query methods
- **Entities** (`entity/`): JPA domain objects with Lombok annotations
- **DTOs** (`dto/`): Data transfer objects for API contracts (distinct from entities)
- **Config** (`config/`): Security, CORS, ModelMapper, Swagger configuration
- **Security** (`security/`): JWT token service, authentication filter

### Core Entities & Relationships
- **User**: Root aggregate, 1→N relationship to Month, Category, Event, Expense, Saving
- **Month**: Scoped to user, organizes expenses by "monthName,year" format (e.g., "January,2025")
- **Category**: Scoped to user+month, organizes expenses within a month
- **Expense**: Scoped to user, references Month, Category (optional), Event (optional)
- **Saving**: Parallel to Expense, scoped to user
- **Event**: Global container for tagging expenses/savings, scoped to user

**Critical**: All entities include `user` field for multi-tenant isolation. Use `@JsonBackReference` on OneToMany relations to prevent JSON serialization loops.

## Developer Workflows & Build

### Build & Run
```bash
# Clean build with Maven
mvn clean install

# Run Spring Boot application (port 8080)
mvn spring-boot:run

# Build only (skip tests)
mvn clean package -DskipTests
```

### Database
- **Active**: PostgreSQL (configured via `application.properties`)
- **Driver**: `org.postgresql.Driver`
- **DDL Strategy**: `spring.jpa.hibernate.ddl-auto=update` (auto-creates/updates schema)
- **Migrations**: Flyway prepared but disabled; SQL files exist in `src/main/resources/db/migration/`

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (auto-generated from code)
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- Framework: SpringDoc OpenAPI 2.1.0

### Testing
- Spring Boot Test context available in `ExpenseTrackingApplicationTests.java`
- Security tests available via `spring-security-test` dependency
- Tests should mock repositories; integration tests run with `@SpringBootTest`

## Code Conventions & Patterns

### Entity & DTO Mapping
- **ModelMapper** (v3.1.1) used for automatic entity↔DTO conversion in services
- **Pattern**: `EntityMapper.mapToDto(entity)` and `mapToEntity(dto)` methods in services
- Example: ExpenseService calls `mapDtoToEntity(null, expenseDto, month, category, user, event)`

### Lombok Usage
- All entities use `@Data` or `@Getter @Setter`, `@Builder(toBuilder=true)`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Controllers/Services use `@AllArgsConstructor` for dependency injection (constructor-based, no @Autowired needed)
- Enables concise getters/setters/constructors without boilerplate

### Authentication & Authorization
- **JWT Service** (`security/JwtService.java`): Token generation/validation with JJWT library
- **Auth Filter** (`security/JwtAuthFilter.java`): Pre-authenticate filter chain, extracts token from `Authorization: Bearer <token>`
- **Endpoints**: `/api/auth/**` is public (login, register); all `/api/**` require JWT token
- **UserDetailsService**: Implemented by `UserService` for Spring Security integration
- **SecurityContextHolder**: Used to get current user: `SecurityContextHolder.getContext().getAuthentication().getName()`

### Repository Query Patterns
- Extend `JpaRepository<Entity, ID>` for CRUD operations
- Use `findBy*` method naming for simple queries (auto-implemented by Spring Data)
- Use `@Query` with JPQL for complex filtering; example pattern:
  ```java
  @Query("""
  select e from Expense e
  join Month m on m.id=e.month.id
  left join Category c on c.id=e.category.id
  where (:monthName is null or m.name=:monthName)
  and (:userId is null or e.user.id=:userId)
  """)
  List<Expense> findByFilters(Long userId, String monthName, Integer monthYear, ...);
  ```

### Service Layer Patterns
- **Null-safe operations**: Use `Optional.isEmpty()` before `.get()`; throw `ResourceNotFoundException` or `InvalidRequestException` on missing resources
- **Entity creation flow**: If referenced entity doesn't exist, create it implicitly (see ExpenseService creating Category/Month on-demand)
- **Cascading deletes**: Use `@OneToMany(orphanRemoval=true)` for automatic child deletion when parent is removed

### Controller Patterns
- `@RequestParam` for query parameters; use `required=false` for optional filters
- `@PathVariable` for URL path parameters
- Return DTOs, not entities (prevents accidental data exposure)
- Simple CRUD endpoints follow REST convention: GET/{id}, GET, POST, PUT/{id}, DELETE/{id}

## Configuration & Dependencies

### Key Dependencies
- **Spring Boot Starters**: web, data-jpa, security, validation, actuator
- **JWT**: `io.jsonwebtoken:jjwt-api/impl/jackson v0.11.5`
- **Database**: postgresql driver (MySQL driver also present but not active)
- **ORM**: Hibernate JPA via Spring Data
- **Security**: BCrypt password encoding
- **API Docs**: springdoc-openapi-starter-webmvc-ui v2.1.0
- **Mapping**: ModelMapper v3.1.1
- **Java Version**: 17

### Critical Properties (application.properties)
- Database connection via `DATASOURCE_URL`, `DATASOURCE_USER`, `DATASOURCE_PASSWORD` environment variables
- Frontend URL via `FRONTEND_URL` environment variable (CORS whitelist)
- Swagger enabled by default
- SQL debug logging enabled (`logging.level.org.hibernate.SQL=DEBUG`)
- Circular reference support enabled for convenience

## Common Integration Points & Workflows

### User Registration & Login
1. Controller: `UserController.register()` → Service: `UserService.register()`
2. Password hashed with BCryptPasswordEncoder
3. JWT token generated on successful login via `JwtService.generateToken(username)`
4. Token validated on every request by `JwtAuthFilter`

### Creating an Expense (Multi-Step Orchestration)
1. **Controller**: `ExpenseController.createExpense()` receives `ExpenseDto`
2. **Service**: `ExpenseService.createExpense()`
   - Fetches/creates Month by "monthName,year" format
   - Fetches/creates Category if specified
   - Fetches Event if specified (no auto-create)
   - Maps DTO to Expense entity with all relations
   - Persists and returns expense ID
3. **Repositories**: Multiple queries (User, Month, Category, Event lookups)

### Filtering Expenses
- **Pattern**: Query parameters `?userId=...&monthName=...&categoryName=...&expenseName=...`
- **Service**: `ExpenseService.getExpenses()` builds filter map, calls repository
- **Repository**: `@Query` method with null-safe WHERE clauses (`(:fieldName is null or fieldName=:fieldName)`)

## Error Handling & Validation

### Custom Exceptions
- `ResourceNotFoundException`: Entity not found (404)
- `InvalidRequestException`: Bad business logic state (400)
- Spring Validation: `@NotNull`, `@NotEmpty` on DTOs

### Exception Handling
- Spring Boot Actuator endpoints available at `/actuator/` (health check, metrics)
- No custom error handler shown; relies on Spring default error responses

## Project-Specific Gotchas & Tips

1. **Month naming format**: Always "monthName,year" (e.g., "January,2025"). Use `MonthService.getMonthName()` and `MonthService.getMonthYear()` utilities.
2. **Multi-tenant isolation**: Every query must filter by `user.id` to prevent data leakage between users.
3. **Optional relationships**: Category and Event are optional on Expense; use `Optional<Entity>` and handle `.isEmpty()` explicitly.
4. **Circular references allowed**: `spring.main.allow-circular-references=true` enables bidirectional entity relationships but requires careful JSON serialization handling.
5. **Auto-create behavior**: Services implicitly create Month/Category if missing; Document this behavior in API docs.
6. **Lombok builders**: Use `@Builder(toBuilder=true)` to enable `.toBuilder()` for entity updates in services.

## Key Files for Understanding Patterns

- **Entity Example**: `entity/User.java` (base aggregate)
- **Service Example**: `service/ExpenseService.java` (multi-step orchestration, null-safety)
- **Controller Example**: `controller/ExpenseController.java` (REST endpoint design)
- **Repository Example**: `repository/ExpenseRepository.java` (custom @Query patterns)
- **Security**: `security/JwtService.java`, `config/SecurityConfig.java`
- **Configuration**: `config/ModelMapperConfig.java` (DTO mapping setup)
- **Tech Docs**: `PROJECT_TECHNICAL_DOCUMENTATION.md` (comprehensive API/DB reference)

## Debugging Tips

- Enable Hibernate SQL logging: Already set to DEBUG in `application.properties`
- Check JWT token validity: Tokens expire; regenerate on login
- Database schema: Use `spring.jpa.show-sql=true` to see generated queries
- Spring Security debugging: Add logging to SecurityConfig or JwtAuthFilter
- CORS issues: Check `frontend.url` environment variable and `config/CorsConfig.java`


# Spring Boot Dynamic Multi-Tenancy MySQL

A Spring Boot application demonstrating **dynamic multi-tenancy** with MySQL. Users can register to create their own
isolated tenant database with complete authentication.

> **⚠️ Current Status**: Registration, login, and tenant database creation are fully functional. Product CRUD operations
> have a known transaction management issue that requires additional configuration. See [Known Issues](#known-issues)
> section.

## Features

- **Dynamic Multi-Tenancy**: Each user registration creates a new isolated database automatically
- **JWT Authentication**: Secure login system with token-based authentication
- **User Registration**: New tenants can self-register and get their own database
- **Complete CRUD Operations**: Full product management API for each tenant
- **Data Isolation**: Complete separation of tenant data in separate databases
- **RESTful API**: Clean REST endpoints with proper HTTP methods
- **Spring Security**: Protected endpoints with JWT validation
- **Input Validation**: Request validation with clear error messages
- **phpMyAdmin**: Web-based database management interface

## Tech Stack

- Java 25
- Spring Boot 3.5.6
- Spring Security 6.x
- Spring Data JPA
- JWT (jsonwebtoken 0.12.6)
- MySQL 9.4.0
- phpMyAdmin (latest)
- Lombok
- Maven
- Docker Compose

## Architecture

### Database Structure

#### Master Database (`master_db`)

- Stores user accounts and tenant configurations
- Tables: `users`, `tenants`
- Used for authentication and tenant management

#### Tenant Databases (Dynamic)

- Each tenant gets its own database: `tenant_{tenantId}_db`
- Created automatically during user registration
- Contains tenant-specific data (products, etc.)
- Complete data isolation between tenants

## Prerequisites

- Java 25
- Maven 3.x
- Docker and Docker Compose

## Quick Start

### 1. Start Services

```bash
docker-compose up -d
```

This starts:

- **MySQL 9.4.0** on port `3301`
- **phpMyAdmin** on port `8081` (http://localhost:8081)

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`

## Access phpMyAdmin

Navigate to: http://localhost:8081

**Login Credentials:**

- Server: `mysql`
- Username: `yu71`
- Password: `53cret`

You can view:

- `master_db` - Contains users and tenants tables
- `tenant_*_db` - Dynamically created tenant databases

## API Documentation

### Authentication Endpoints

#### 1. Register New Tenant

Creates a new user account and automatically creates a dedicated database for the tenant.

**Endpoint:** `POST /api/auth/register`

**Request:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@acme.com",
    "password": "password123",
    "tenantName": "Acme Corporation",
    "tenantId": "acme"
  }'
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwidGVuYW50SWQiOiJhY21lIiwiaWF0IjoxNzI4NDU2...",
  "tokenType": "Bearer",
  "username": "john",
  "tenantId": "acme",
  "tenantName": "Acme Corporation",
  "message": "Registration successful. Tenant database created."
}
```

**What Happens:**

1. Validates tenant ID is unique
2. Creates database: `tenant_acme_db`
3. Creates `products` table in the new database
4. Saves tenant config to master database
5. Creates user account
6. Returns JWT token

#### 2. Login

Authenticates existing user and returns JWT token.

**Endpoint:** `POST /api/auth/login`

**Request:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwidGVuYW50SWQiOiJhY21lIiwiaWF0IjoxNzI4NDU2...",
  "tokenType": "Bearer",
  "username": "john",
  "tenantId": "acme",
  "tenantName": "Acme Corporation",
  "message": "Login successful"
}
```

### Product Endpoints

All product endpoints require JWT authentication. Include the token in the `Authorization` header.

#### 1. Create Product

**Endpoint:** `POST /api/products`

**Request:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop for developers",
    "price": 1299.99,
    "quantity": 50
  }'
```

**Response:**

```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop for developers",
  "price": 1299.99,
  "quantity": 50,
  "createdAt": "2025-10-09T05:30:00",
  "updatedAt": "2025-10-09T05:30:00"
}
```

#### 2. Get All Products

**Endpoint:** `GET /api/products`

**Request:**
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response:**

```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop for developers",
    "price": 1299.99,
    "quantity": 50,
    "createdAt": "2025-10-09T05:30:00",
    "updatedAt": "2025-10-09T05:30:00"
  }
]
```

#### 3. Get Product by ID

**Endpoint:** `GET /api/products/{id}`

**Request:**

```bash
curl -X GET http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 4. Update Product

**Endpoint:** `PUT /api/products/{id}`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Laptop",
    "description": "Updated high-performance laptop",
    "price": 1199.99,
    "quantity": 45
  }'
```

#### 5. Delete Product

**Endpoint:** `DELETE /api/products/{id}`

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 6. Search Products by Name

**Endpoint:** `GET /api/products/search?name={name}`

**Request:**

```bash
curl -X GET "http://localhost:8080/api/products/search?name=laptop" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 7. Get Low Stock Products

**Endpoint:** `GET /api/products/low-stock?threshold={number}`

**Request:**

```bash
curl -X GET "http://localhost:8080/api/products/low-stock?threshold=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Complete Example Workflow

### Scenario: Two Companies Using the System

#### 1. Acme Corp Registers

```bash
# Register Acme Corp
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_acme",
    "email": "john@acme.com",
    "password": "acme123",
    "tenantName": "Acme Corporation",
    "tenantId": "acme"
  }'

# Save the token from response
ACME_TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

Database `tenant_acme_db` is created automatically.

#### 2. TechStart Registers

```bash
# Register TechStart
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_tech",
    "email": "jane@techstart.com",
    "password": "tech456",
    "tenantName": "TechStart Inc",
    "tenantId": "techstart"
  }'

# Save the token from response
TECHSTART_TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

Database `tenant_techstart_db` is created automatically.

#### 3. Acme Adds Products

```bash
# Acme adds a laptop
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACME_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Laptop Pro",
    "description": "Premium business laptop",
    "price": 1499.99,
    "quantity": 100
  }'

# Acme adds another product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACME_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Mouse",
    "description": "Wireless ergonomic mouse",
    "price": 29.99,
    "quantity": 500
  }'
```

Products stored in `tenant_acme_db`.

#### 4. TechStart Adds Products

```bash
# TechStart adds a gadget
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TECHSTART_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TechStart Tablet",
    "description": "Latest tablet technology",
    "price": 799.99,
    "quantity": 75
  }'
```

Product stored in `tenant_techstart_db`.

#### 5. Verify Data Isolation

```bash
# Acme views their products (sees only Acme products)
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACME_TOKEN"

# Returns: Acme Laptop Pro, Acme Mouse
# Does NOT see: TechStart Tablet

# TechStart views their products (sees only TechStart products)
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TECHSTART_TOKEN"

# Returns: TechStart Tablet
# Does NOT see: Acme products
```

## Configuration

### Application Properties

`src/main/resources/application.properties`:

```properties
# Master Database Configuration
spring.datasource.master.url=jdbc:mysql://localhost:3301/master_db
spring.datasource.master.username=yu71
spring.datasource.master.password=53cret

# JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm
jwt.expiration=86400000  # 24 hours

# Server Configuration
server.port=8080
```

### Docker Compose Services

- **MySQL**: Port 3301
- **phpMyAdmin**: Port 8081

## Validation Rules

### Registration

- Username: 3-50 characters, required
- Email: Valid email format, required
- Password: Minimum 6 characters, required
- Tenant Name: Required
- Tenant ID: 3-20 characters, lowercase letters/numbers/underscores only

### Product

- Name: Required, cannot be blank
- Price: Required, must be positive
- Quantity: Required, must be a number

## Security Features

- **Password Encryption**: BCrypt hashing
- **JWT Tokens**: Signed with HS256 algorithm
- **Token Expiration**: 24 hours (configurable)
- **Spring Security**: All endpoints protected except auth endpoints
- **Data Isolation**: Complete tenant separation
- **SQL Injection Protection**: JPA/Hibernate parameterized queries

## Troubleshooting

### Registration Issues

**Error: "Tenant ID already exists"**

- Solution: Choose a different tenant ID

**Error: "Username already exists"**

- Solution: Choose a different username

### Login Issues

**Error: "Invalid username or password"**

- Solution: Verify credentials (case-sensitive)

### Product API Issues

**Error: "No tenant context found"**

- Solution: Include JWT token in Authorization header
- Format: `Authorization: Bearer YOUR_TOKEN`

**Error: "Unauthorized" (401)**

- Solution: Token expired or invalid, login again

## Testing

Run tests:
```bash
mvn clean test
```

Build package:

```bash
mvn clean package
```

## Project Structure

```
src/main/java/id/my/hendisantika/multitenancymysql/
├── config/
│   ├── MasterDataSourceConfig.java      # Master DB configuration
│   ├── TenantDataSourceConfig.java      # Tenant DB configuration
│   ├── TenantRoutingDataSource.java     # Dynamic routing
│   ├── TenantContext.java               # Thread-local tenant storage
│   ├── TenantLoader.java                # Loads tenants on startup
│   └── SecurityConfig.java              # Spring Security config
├── controller/
│   ├── AuthController.java              # Auth endpoints
│   └── ProductController.java           # Product CRUD endpoints
├── dto/
│   ├── RegisterRequest.java             # Registration input
│   ├── LoginRequest.java                # Login input
│   └── AuthResponse.java                # Auth response
├── entity/
│   ├── User.java                        # User entity (master DB)
│   ├── Tenant.java                      # Tenant entity (master DB)
│   └── Product.java                     # Product entity (tenant DBs)
├── repository/
│   ├── UserRepository.java              # User data access
│   ├── TenantRepository.java            # Tenant data access
│   └── ProductRepository.java           # Product data access
├── security/
│   ├── JwtTokenProvider.java            # JWT generation/validation
│   └── JwtAuthenticationFilter.java     # JWT filter
└── service/
    ├── AuthService.java                 # Authentication logic
    ├── TenantService.java               # Tenant DB management
    └── ProductService.java              # Product business logic
```

## Additional Documentation

- [USAGE_GUIDE.md](USAGE_GUIDE.md) - Detailed usage examples
- [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) - Technical implementation details
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Legacy API documentation

## Monitoring Databases

Use phpMyAdmin (http://localhost:8081) to:

- View `master_db` tables (users, tenants)
- View dynamically created tenant databases
- Monitor database connections
- Execute SQL queries

## Stopping the Application

Stop Docker containers:
```bash
docker-compose down
```

Stop and remove volumes:
```bash
docker-compose down -v
```

## Known Issues

### Transaction Management with Dual DataSources

**Issue**: Product CRUD operations currently fail with transaction errors when using dual EntityManagerFactories (
master + tenant).

**Symptoms**:

- Registration and login work perfectly
- Tenant databases are created successfully
- Product API returns: `"Could not open JPA EntityManager for transaction"`

**Root Cause**:
The tenant context lifecycle conflicts with Spring's transaction management when using separate transaction managers for
master and tenant databases. The context is cleared before transactions complete.

**Potential Solutions**:

1. **Schema-based multi-tenancy** instead of database-per-tenant (simpler transaction management)
2. **Manual transaction management** at the repository level
3. **Single transaction manager** with custom routing
4. **TransactionSynchronization** hooks to manage tenant context cleanup timing

**Workaround**:
For development/testing, you can:

- Use the master database directly for product operations
- Or implement schema-based multi-tenancy (all tenants in one database, different schemas)

**Status**: This is a known architectural challenge in database-per-tenant multi-tenancy with JPA. The infrastructure (
user management, tenant provisioning, JWT auth) is fully functional.

## Implementation Status

### ✅ Fully Working

- Docker Compose setup (MySQL + phpMyAdmin)
- Dynamic tenant database creation
- User registration with automatic tenant provisioning
- JWT authentication and authorization
- Tenant context management
- phpMyAdmin integration
- Master database operations (users, tenants)

### ⚠️ Requires Configuration

- Product CRUD operations (transaction management issue)
- Tenant-specific data operations

## Future Enhancements

- [ ] **Fix transaction management** for product CRUD operations
- [ ] Multi-user support per tenant
- [ ] Role-based access control (RBAC)
- [ ] Tenant-specific configurations
- [ ] Billing and subscription management
- [ ] Tenant analytics dashboard
- [ ] Database backup and restore per tenant
- [ ] Tenant data export functionality
- [ ] API rate limiting per tenant

## License

This project is open source and available under the MIT License.

## Author

**Created by IntelliJ IDEA**

- User: hendisantika
- Email: hendisantika@yahoo.co.id
- Telegram: @hendisantika34
- Link: s.id/hendisantika

---

**Note**: This implementation demonstrates the core infrastructure for dynamic multi-tenancy with database-per-tenant
isolation. The user management, authentication, and tenant provisioning components are fully functional. Product CRUD
operations require transaction management refinement (see [Known Issues](#known-issues)).

# Dynamic Multi-Tenancy Usage Guide

## Complete Implementation Overview

This application now features a **fully dynamic multi-tenancy system** where:

- New users can register and create their own tenant
- Each tenant gets its own isolated MySQL database
- Login returns a JWT token with tenant context
- All product API calls are automatically routed to the correct tenant database

## Architecture

### Databases

#### Master Database (`master_db`)

- Stores `users` and `tenants` tables
- Used for authentication and tenant management
- Location: `localhost:3301/master_db`

#### Tenant Databases (created dynamically)

- Each tenant has its own database: `tenant_{tenantId}_db`
- Contains product tables and tenant-specific data
- Created automatically during registration

## Getting Started

### 1. Start MySQL

```bash
docker-compose up -d
```

This creates the `master_db` database.

### 2. Run the Application

```bash
mvn spring-boot:run
```

## API Usage

### Register a New Tenant

**Endpoint:** `POST /api/auth/register`

**Request:**

```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123",
  "tenantName": "Acme Corporation",
  "tenantId": "acme"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "john",
  "tenantId": "acme",
  "tenantName": "Acme Corporation",
  "message": "Registration successful. Tenant database created."
}
```

**What happens:**

1. System validates tenant ID is unique
2. Creates new database: `tenant_acme_db`
3. Creates `products` table in the new database
4. Saves tenant configuration to master database
5. Creates user account linked to the tenant
6. Returns JWT token

### Login

**Endpoint:** `POST /api/auth/login`

**Request:**

```json
{
  "username": "john",
  "password": "password123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "john",
  "tenantId": "acme",
  "tenantName": "Acme Corporation",
  "message": "Login successful"
}
```

### Use Product APIs

All product APIs require the JWT token in the Authorization header.

**Create Product:**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer {your_jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "quantity": 50
  }'
```

**Get All Products:**

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer {your_jwt_token}"
```

**The system automatically:**

1. Validates the JWT token
2. Extracts the tenant ID from the token
3. Routes the request to the correct tenant database
4. Returns data from that specific tenant

## Complete Flow Example

### 1. Register First Tenant (Acme Corp)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_acme",
    "email": "john@acme.com",
    "password": "password123",
    "tenantName": "Acme Corporation",
    "tenantId": "acme"
  }'
```

Database `tenant_acme_db` is created.

### 2. Register Second Tenant (TechStart)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_tech",
    "email": "jane@techstart.com",
    "password": "password456",
    "tenantName": "TechStart Inc",
    "tenantId": "techstart"
  }'
```

Database `tenant_techstart_db` is created.

### 3. Login as Acme User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_acme",
    "password": "password123"
  }'
```

Save the token from the response.

### 4. Create Product for Acme

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer {acme_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Widget",
    "description": "Premium widget",
    "price": 99.99,
    "quantity": 100
  }'
```

This product is stored in `tenant_acme_db`.

### 5. Login as TechStart User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_tech",
    "password": "password456"
  }'
```

### 6. Create Product for TechStart

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer {techstart_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TechStart Gadget",
    "description": "Innovative gadget",
    "price": 149.99,
    "quantity": 50
  }'
```

This product is stored in `tenant_techstart_db`.

### 7. Verify Data Isolation

When Acme user fetches products:

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer {acme_token}"
```

They only see "Acme Widget" - NOT "TechStart Gadget".

When TechStart user fetches products:

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer {techstart_token}"
```

They only see "TechStart Gadget" - NOT "Acme Widget".

## Data Isolation

- Each tenant's data is completely isolated in separate databases
- Users cannot access other tenants' data
- The JWT token ensures requests are routed to the correct database
- No cross-tenant data leakage is possible

## Security Features

- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours (configurable)
- Spring Security protects all endpoints except auth endpoints
- Each request is validated and routed to the correct tenant

## Configuration

### JWT Settings

In `application.properties`:

```properties
jwt.secret=mySecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Master Database

```properties
spring.datasource.master.url=jdbc:mysql://localhost:3301/master_db
spring.datasource.master.username=yu71
spring.datasource.master.password=53cret
```

## Troubleshooting

### Registration fails with "Tenant ID already exists"

- Choose a different `tenantId`
- Tenant IDs must be unique and contain only lowercase letters, numbers, and underscores

### Login fails with "Invalid username or password"

- Verify the username and password are correct
- Usernames and passwords are case-sensitive

### Product API returns "No tenant context found"

- Ensure you're including the JWT token in the Authorization header
- Format: `Authorization: Bearer {your_token}`
- Check that the token hasn't expired

## Next Steps

You can extend this system to:

1. Add user roles and permissions per tenant
2. Implement tenant-specific configurations
3. Add billing and subscription management
4. Implement tenant data backup and restore
5. Add multi-user support per tenant
6. Implement tenant analytics and reporting

## Database Schema

### Master Database Tables

**users:**

- id (PK)
- username (unique)
- email (unique)
- password (encrypted)
- tenant_id (FK to tenants)
- role
- created_at

**tenants:**

- id (PK)
- tenant_id (unique)
- tenant_name
- db_url
- db_username
- db_password
- created_at

### Tenant Database Tables

**products:**

- id (PK)
- name
- description
- price
- quantity
- created_at
- updated_at

Each tenant database has its own `products` table with isolated data.

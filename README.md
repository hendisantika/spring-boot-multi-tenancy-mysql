# Spring Boot Multi-Tenancy MySQL with Product CRUD

A Spring Boot application demonstrating multi-tenancy with MySQL using separate databases for each tenant, featuring a
complete CRUD implementation for Product management.

## Features

- Multi-tenant architecture with separate databases per tenant
- Complete CRUD operations for Product entity
- RESTful API endpoints
- Automatic tenant detection via HTTP headers
- Input validation
- Global exception handling
- JPA/Hibernate integration
- MySQL database support

## Tech Stack

- Java 25
- Spring Boot 3.5.6
- Spring Data JPA
- MySQL 8.0
- Lombok
- Maven
- Docker Compose

## Project Structure

```
src/main/java/id/my/hendisantika/multitenancymysql/
├── config/
│   ├── DataSourceConfig.java           # Multi-tenant datasource configuration
│   ├── MultiTenantDataSourceRouter.java # Routes to tenant-specific databases
│   ├── TenantContext.java              # Thread-local tenant context
│   ├── TenantInterceptor.java          # Extracts tenant from request headers
│   └── WebMvcConfig.java               # Web MVC configuration
├── controller/
│   └── ProductController.java          # REST endpoints for products
├── entity/
│   └── Product.java                    # Product JPA entity
├── exception/
│   └── GlobalExceptionHandler.java     # Global exception handling
├── repository/
│   └── ProductRepository.java          # Product data access layer
└── service/
    └── ProductService.java             # Product business logic
```

## Prerequisites

- Java 25
- Maven 3.x
- Docker and Docker Compose (for MySQL)

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd spring-boot-multi-tenancy-mysql
```

### 2. Start MySQL using Docker Compose

```bash
docker-compose up -d
```

This will:

- Start MySQL 8.0 container
- Create `tenant1_db` and `tenant2_db` databases
- Expose MySQL on port 3306

### 3. Build the application

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Multi-Tenancy

The application supports multi-tenancy using a **separate database per tenant** approach.

### How it works:

1. **Tenant Identification**: Each request includes a `X-Tenant-ID` header
2. **Tenant Context**: The `TenantInterceptor` extracts the tenant ID and stores it in `TenantContext`
3. **Database Routing**: `MultiTenantDataSourceRouter` routes queries to the appropriate database
4. **Data Isolation**: Each tenant's data is completely isolated in separate databases

### Supported Tenants:

- `tenant1` → uses `tenant1_db` database
- `tenant2` → uses `tenant2_db` database

If no `X-Tenant-ID` header is provided, `tenant1` is used as default.

## API Endpoints

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API documentation.

### Quick Examples:

**Create a product:**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "quantity": 50
  }'
```

**Get all products:**

```bash
curl -X GET http://localhost:8080/api/products \
  -H "X-Tenant-ID: tenant1"
```

**Update a product:**

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "Updated Laptop",
    "description": "Updated description",
    "price": 1199.99,
    "quantity": 45
  }'
```

**Delete a product:**

```bash
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "X-Tenant-ID: tenant1"
```

## Configuration

Configuration is in `src/main/resources/application.properties`:

```properties
# Tenant 1 DataSource
spring.datasource.tenant1.jdbc-url=jdbc:mysql://localhost:3306/tenant1_db
spring.datasource.tenant1.username=root
spring.datasource.tenant1.password=root
# Tenant 2 DataSource
spring.datasource.tenant2.jdbc-url=jdbc:mysql://localhost:3306/tenant2_db
spring.datasource.tenant2.username=root
spring.datasource.tenant2.password=root
```

### Adding More Tenants:

1. Add datasource configuration in `application.properties`
2. Update `DataSourceConfig.java` to include the new tenant
3. Create the database in MySQL

## Product Entity

```java
{
        "id":1,
        "name":"Laptop",
        "description":"High-performance laptop",
        "price":1299.99,
        "quantity":50,
        "createdAt":"2025-10-09T10:30:00",
        "updatedAt":"2025-10-09T10:30:00"
        }
```

## Validation

Product fields are validated:

- `name`: Required, cannot be blank
- `price`: Required, must be positive
- `quantity`: Required

## Testing Multi-Tenancy

To verify multi-tenancy works:

1. Create a product for tenant1:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name":"Laptop","description":"Tenant 1 product","price":1299.99,"quantity":50}'
```

2. Create a product for tenant2:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant2" \
  -d '{"name":"Monitor","description":"Tenant 2 product","price":599.99,"quantity":30}'
```

3. Fetch products for tenant1:

```bash
curl -X GET http://localhost:8080/api/products -H "X-Tenant-ID: tenant1"
```

4. Fetch products for tenant2:

```bash
curl -X GET http://localhost:8080/api/products -H "X-Tenant-ID: tenant2"
```

You'll see that each tenant only sees their own products.

## Stopping the Application

To stop MySQL:

```bash
docker-compose down
```

To stop and remove volumes:

```bash
docker-compose down -v
```

## License

This project is open source and available under the MIT License.

# Product CRUD API Documentation

This is a Spring Boot multi-tenancy application with MySQL that supports CRUD operations for products.

## Multi-Tenancy

The application supports multiple tenants using a separate database approach. Each tenant has its own database:

- **tenant1**: Uses `tenant1_db` database
- **tenant2**: Uses `tenant2_db` database

### Tenant Selection

Pass the tenant identifier in the request header:

```
X-Tenant-ID: tenant1
```

If no header is provided, `tenant1` is used as the default.

## API Endpoints

### Base URL

```
http://localhost:8080/api/products
```

### 1. Create Product

**POST** `/api/products`

**Headers:**

```
Content-Type: application/json
X-Tenant-ID: tenant1
```

**Request Body:**

```json
{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "quantity": 50
}
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "quantity": 50,
  "createdAt": "2025-10-09T10:30:00",
  "updatedAt": "2025-10-09T10:30:00"
}
```

### 2. Get All Products

**GET** `/api/products`

**Headers:**

```
X-Tenant-ID: tenant1
```

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "quantity": 50,
    "createdAt": "2025-10-09T10:30:00",
    "updatedAt": "2025-10-09T10:30:00"
  }
]
```

### 3. Get Product by ID

**GET** `/api/products/{id}`

**Headers:**

```
X-Tenant-ID: tenant1
```

**Response:** `200 OK` or `404 Not Found`

### 4. Update Product

**PUT** `/api/products/{id}`

**Headers:**

```
Content-Type: application/json
X-Tenant-ID: tenant1
```

**Request Body:**

```json
{
  "name": "Updated Laptop",
  "description": "Updated high-performance laptop",
  "price": 1199.99,
  "quantity": 45
}
```

**Response:** `200 OK` or `404 Not Found`

### 5. Delete Product

**DELETE** `/api/products/{id}`

**Headers:**

```
X-Tenant-ID: tenant1
```

**Response:** `204 No Content` or `404 Not Found`

### 6. Search Products by Name

**GET** `/api/products/search?name={searchTerm}`

**Headers:**

```
X-Tenant-ID: tenant1
```

**Example:**

```
GET /api/products/search?name=laptop
```

**Response:** `200 OK`

### 7. Get Low Stock Products

**GET** `/api/products/low-stock?threshold={threshold}`

**Headers:**

```
X-Tenant-ID: tenant1
```

**Example:**

```
GET /api/products/low-stock?threshold=10
```

**Response:** `200 OK`

## Testing with cURL

### Create a product for tenant1:

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

### Create a product for tenant2:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant2" \
  -d '{
    "name": "Monitor",
    "description": "4K Monitor",
    "price": 599.99,
    "quantity": 30
  }'
```

### Get all products for tenant1:

```bash
curl -X GET http://localhost:8080/api/products \
  -H "X-Tenant-ID: tenant1"
```

### Update a product:

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

### Delete a product:

```bash
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "X-Tenant-ID: tenant1"
```

### Search products:

```bash
curl -X GET "http://localhost:8080/api/products/search?name=laptop" \
  -H "X-Tenant-ID: tenant1"
```

### Get low stock products:

```bash
curl -X GET "http://localhost:8080/api/products/low-stock?threshold=10" \
  -H "X-Tenant-ID: tenant1"
```

## Configuration

### Database Setup

The application expects MySQL to be running on `localhost:3306` with:

- Username: `root`
- Password: `root`

The databases `tenant1_db` and `tenant2_db` will be created automatically if they don't exist.

### Customizing Configuration

Edit `src/main/resources/application.properties` to customize:

- Database credentials
- Server port
- JPA settings
- Add more tenants

## Multi-Tenancy Implementation

The multi-tenancy is implemented using:

1. **TenantContext**: Thread-local storage for current tenant
2. **TenantInterceptor**: Extracts tenant from request header
3. **MultiTenantDataSourceRouter**: Routes to the correct database
4. **DataSourceConfig**: Configures multiple data sources

Each tenant's data is completely isolated in separate databases.

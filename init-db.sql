-- Initialize databases for multi-tenancy

CREATE DATABASE IF NOT EXISTS tenant1_db;
CREATE DATABASE IF NOT EXISTS tenant2_db;

-- Grant permissions (optional, root already has all permissions)
GRANT ALL PRIVILEGES ON tenant1_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON tenant2_db.* TO 'root'@'%';

FLUSH PRIVILEGES;

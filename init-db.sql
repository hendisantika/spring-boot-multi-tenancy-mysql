-- Initialize master database for multi-tenancy

CREATE DATABASE IF NOT EXISTS master_db;

-- Grant permissions to yu71 user
GRANT ALL PRIVILEGES ON *.* TO 'yu71'@'%' WITH GRANT OPTION;

FLUSH PRIVILEGES;

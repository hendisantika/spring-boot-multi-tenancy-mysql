package id.my.hendisantika.multitenancymysql.service;

import com.zaxxer.hikari.HikariDataSource;
import id.my.hendisantika.multitenancymysql.entity.Tenant;
import id.my.hendisantika.multitenancymysql.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.35
 * To change this template use File | Settings | File Templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    @Value("${spring.datasource.master.jdbc-url}")
    private String masterDbUrl;
    @Value("${spring.datasource.master.username}")
    private String masterUsername;
    @Value("${spring.datasource.master.password}")
    private String masterPassword;

    @Transactional
    public Tenant createTenant(String tenantId, String tenantName) {
        if (tenantRepository.existsByTenantId(tenantId)) {
            throw new RuntimeException("Tenant ID already exists: " + tenantId);
        }

        // Create the database
        String dbName = "tenant_" + tenantId + "_db";
        createDatabase(dbName);

        // Create tenant datasource configuration
        String dbUrl = masterDbUrl.substring(0, masterDbUrl.lastIndexOf('/') + 1) + dbName;

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantName(tenantName);
        tenant.setDbUrl(dbUrl);
        tenant.setDbUsername(masterUsername);
        tenant.setDbPassword(masterPassword);

        tenant = tenantRepository.save(tenant);

        // Initialize tenant datasource
        initializeTenantDataSource(tenant);

        // Create schema in tenant database
        createTenantSchema(tenantId);

        log.info("Created new tenant: {} with database: {}", tenantId, dbName);
        return tenant;
    }

    private void createDatabase(String dbName) {
        try (Connection connection = createMasterConnection();
             Statement statement = connection.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS " + dbName +
                        " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            statement.executeUpdate(sql);

            log.info("Database created: {}", dbName);
        } catch (Exception e) {
            log.error("Failed to create database: {}", dbName, e);
            throw new RuntimeException("Failed to create tenant database", e);
        }
    }

    private void createTenantSchema(String tenantId) {
        DataSource dataSource = tenantDataSources.get(tenantId);
        if (dataSource == null) {
            throw new RuntimeException("DataSource not found for tenant: " + tenantId);
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Create products table in tenant database
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description VARCHAR(1000),
                    price DECIMAL(38,2) NOT NULL,
                    quantity INTEGER NOT NULL,
                    created_at DATETIME(6) NOT NULL,
                    updated_at DATETIME(6)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;

            statement.executeUpdate(createTableSQL);
            log.info("Schema created for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("Failed to create schema for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to create tenant schema", e);
        }
    }

    private Connection createMasterConnection() throws Exception {
        // Extract base URL without database name
        String baseUrl = masterDbUrl.substring(0, masterDbUrl.lastIndexOf('/'));

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(baseUrl);
        ds.setUsername(masterUsername);
        ds.setPassword(masterPassword);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return ds.getConnection();
    }

    public void initializeTenantDataSource(Tenant tenant) {
        if (tenantDataSources.containsKey(tenant.getTenantId())) {
            return;
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(tenant.getDbUrl());
        ds.setUsername(tenant.getDbUsername());
        ds.setPassword(tenant.getDbPassword());
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(2);

        tenantDataSources.put(tenant.getTenantId(), ds);
        log.info("Initialized datasource for tenant: {}", tenant.getTenantId());
    }

    public DataSource getTenantDataSource(String tenantId) {
        DataSource dataSource = tenantDataSources.get(tenantId);

        if (dataSource == null) {
            // Load tenant from database and initialize
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
            initializeTenantDataSource(tenant);
            dataSource = tenantDataSources.get(tenantId);
        }

        return dataSource;
    }

    public void loadAllTenants() {
        tenantRepository.findAll().forEach(this::initializeTenantDataSource);
        log.info("Loaded {} tenant datasources", tenantDataSources.size());
    }
}

package id.my.hendisantika.multitenancymysql.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 08.16
 * To change this template use File | Settings | File Templates.
 */
@Component
@Slf4j
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(@Qualifier("tenantDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.debug("Getting connection for tenant: {}", tenantIdentifier);
        final Connection connection = getAnyConnection();

        if (tenantIdentifier != null && !"default".equals(tenantIdentifier)) {
            try {
                String databaseName = "tenant_" + tenantIdentifier + "_db";
                connection.setCatalog(databaseName);
                log.debug("Switched to database: {}", databaseName);
            } catch (SQLException e) {
                log.error("Could not switch to tenant database: {}", tenantIdentifier, e);
                throw e;
            }
        }

        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        log.debug("Releasing connection for tenant: {}", tenantIdentifier);
        // Reset to master database before releasing
        try {
            connection.setCatalog("master_db");
        } catch (SQLException e) {
            log.warn("Could not reset catalog to master_db", e);
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}

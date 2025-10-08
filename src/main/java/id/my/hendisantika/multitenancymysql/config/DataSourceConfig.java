package id.my.hendisantika.multitenancymysql.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.18
 * To change this template use File | Settings | File Templates.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.tenant1")
    public DataSource tenant1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.tenant2")
    public DataSource tenant2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource dataSource() {
        MultiTenantDataSourceRouter router = new MultiTenantDataSourceRouter();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("tenant1", tenant1DataSource());
        targetDataSources.put("tenant2", tenant2DataSource());

        router.setTargetDataSources(targetDataSources);
        router.setDefaultTargetDataSource(tenant1DataSource());
        router.afterPropertiesSet();

        return router;
    }
}

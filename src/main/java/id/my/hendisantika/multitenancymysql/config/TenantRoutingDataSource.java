package id.my.hendisantika.multitenancymysql.config;

import id.my.hendisantika.multitenancymysql.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.38
 * To change this template use File | Settings | File Templates.
 */
@RequiredArgsConstructor
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final TenantService tenantService;

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    protected javax.sql.DataSource determineTargetDataSource() {
        String tenantId = (String) determineCurrentLookupKey();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        return tenantService.getTenantDataSource(tenantId);
    }
}

package id.my.hendisantika.multitenancymysql.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 08.15
 * To change this template use File | Settings | File Templates.
 */
@Component
@Slf4j
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getCurrentTenant();
        String resolved = tenantId != null ? tenantId : DEFAULT_TENANT;
        log.debug("Resolving tenant identifier: {} (from context: {})", resolved, tenantId);
        return resolved;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

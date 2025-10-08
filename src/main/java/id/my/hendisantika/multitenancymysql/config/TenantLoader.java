package id.my.hendisantika.multitenancymysql.config;

import id.my.hendisantika.multitenancymysql.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.42
 * To change this template use File | Settings | File Templates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantLoader {

    private final TenantService tenantService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadAllTenants() {
        log.info("Loading all tenant datasources...");
        tenantService.loadAllTenants();
        log.info("All tenant datasources loaded successfully");
    }
}

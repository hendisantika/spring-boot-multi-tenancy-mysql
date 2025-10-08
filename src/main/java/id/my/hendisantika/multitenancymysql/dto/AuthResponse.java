package id.my.hendisantika.multitenancymysql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.32
 * To change this template use File | Settings | File Templates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String tenantId;
    private String tenantName;
    private String message;

    public AuthResponse(String token, String username, String tenantId, String tenantName) {
        this.token = token;
        this.username = username;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
    }
}

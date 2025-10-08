package id.my.hendisantika.multitenancymysql.service;

import id.my.hendisantika.multitenancymysql.dto.AuthResponse;
import id.my.hendisantika.multitenancymysql.dto.LoginRequest;
import id.my.hendisantika.multitenancymysql.dto.RegisterRequest;
import id.my.hendisantika.multitenancymysql.entity.Tenant;
import id.my.hendisantika.multitenancymysql.entity.User;
import id.my.hendisantika.multitenancymysql.repository.TenantRepository;
import id.my.hendisantika.multitenancymysql.repository.UserRepository;
import id.my.hendisantika.multitenancymysql.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.39
 * To change this template use File | Settings | File Templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate username and email are unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create tenant and database
        Tenant tenant = tenantService.createTenant(request.getTenantId(), request.getTenantName());

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenantId(tenant.getTenantId());
        user.setRole("USER");

        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getTenantId());

        log.info("User registered successfully: {} for tenant: {}", user.getUsername(), tenant.getTenantId());

        return new AuthResponse(
                token,
                user.getUsername(),
                tenant.getTenantId(),
                tenant.getTenantName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Get tenant information
        Tenant tenant = tenantRepository.findByTenantId(user.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getTenantId());

        log.info("User logged in successfully: {} for tenant: {}", user.getUsername(), tenant.getTenantId());

        return new AuthResponse(
                token,
                user.getUsername(),
                tenant.getTenantId(),
                tenant.getTenantName()
        );
    }
}

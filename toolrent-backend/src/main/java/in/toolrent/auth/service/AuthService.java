package in.toolrent.auth.service;

import in.toolrent.auth.dto.AuthResponse;
import in.toolrent.auth.dto.LoginRequest;
import in.toolrent.auth.dto.RegisterCustomerRequest;
import in.toolrent.auth.dto.RegisterTenantRequest;
import in.toolrent.auth.entity.User;
import in.toolrent.auth.repository.UserRepository;
import in.toolrent.tenant.context.TenantContext;
import in.toolrent.tenant.entity.Tenant;
import in.toolrent.tenant.repository.TenantRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtService       jwtService;

    private Tenant currentTenantOrNull() {
        String subdomain = TenantContext.getCurrentTenant();
        if (subdomain == null || subdomain.isBlank()) {
            return null;
        }
        return tenantRepository.findBySubdomain(subdomain).orElse(null);
    }

    @Transactional
    public AuthResponse registerTenant(RegisterTenantRequest request) {
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new IllegalArgumentException("Subdomain '" + request.getSubdomain() + "' is already taken");
        }
        if (tenantRepository.existsByOwnerEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .businessName(request.getBusinessName())
                .subdomain(request.getSubdomain().toLowerCase().trim())
                .ownerEmail(request.getEmail())
                .plan("FREE")
                .build());

        User user = userRepository.save(User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.TENANT_ADMIN)
                .build());

        log.info("Registered new tenant: {} ({})", tenant.getBusinessName(), tenant.getSubdomain());
        return buildAuthResponse(user, tenant);
    }

    @Transactional
    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        Tenant tenant = currentTenantOrNull();
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant context is required for customer registration");
        }
        if (userRepository.existsByEmailAndTenant(request.getEmail(), tenant)) {
            throw new IllegalArgumentException("Email is already registered for this tenant");
        }

        User user = userRepository.save(User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.CUSTOMER)
                .build());

        return buildAuthResponse(user, tenant);
    }

    public AuthResponse login(LoginRequest request) {
        Tenant tenant = currentTenantOrNull();
        User user;
        if (tenant != null) {
            user = userRepository.findByEmailAndTenant(request.getEmail(), tenant)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        return buildAuthResponse(user, user.getTenant());
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Claims claims = jwtService.parseToken(refreshToken);
        if (!Objects.equals("refresh", claims.get("type", String.class))) {
            throw new IllegalArgumentException("Invalid token type");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        return buildAuthResponse(user, user.getTenant());
    }

    private AuthResponse buildAuthResponse(User user, Tenant tenant) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(tenant != null ? tenant.getId() : null)
                .subdomain(tenant != null ? tenant.getSubdomain() : null)
                .businessName(tenant != null ? tenant.getBusinessName() : null)
                .build();
    }
}

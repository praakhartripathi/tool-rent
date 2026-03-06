package in.toolrent.auth.controller;

import in.toolrent.auth.dto.AuthResponse;
import in.toolrent.auth.dto.LoginRequest;
import in.toolrent.auth.dto.RefreshTokenRequest;
import in.toolrent.auth.dto.RegisterCustomerRequest;
import in.toolrent.auth.dto.RegisterTenantRequest;
import in.toolrent.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register-tenant
     * Registers a new rental business and returns JWT tokens.
     */
    @PostMapping("/register-tenant")
    public ResponseEntity<AuthResponse> registerTenant(
            @Valid @RequestBody RegisterTenantRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerTenant(request));
    }

    /**
     * POST /api/auth/register-customer
     * Registers a customer under the currently resolved tenant.
     */
    @PostMapping("/register-customer")
    public ResponseEntity<AuthResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerCustomer(request));
    }

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/refresh
     * Body: { "refreshToken": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    /**
     * GET /api/auth/health
     * Health check endpoint used by Docker Compose.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}

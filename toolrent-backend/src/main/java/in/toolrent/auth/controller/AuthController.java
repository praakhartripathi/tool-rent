package in.toolrent.auth.controller;

import in.toolrent.auth.dto.AuthResponse;
import in.toolrent.auth.dto.LoginRequest;
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
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        // TODO: implement refresh token rotation using refresh_tokens table
        return ResponseEntity.ok(Map.of("message", "Refresh endpoint — implement refresh token rotation"));
    }
}

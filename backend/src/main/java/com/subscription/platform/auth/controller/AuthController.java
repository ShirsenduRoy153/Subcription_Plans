package com.subscription.platform.auth.controller;

import com.subscription.platform.auth.dto.AuthResponse;
import com.subscription.platform.auth.dto.LoginRequest;
import com.subscription.platform.auth.dto.UserRegisterRequest;
import com.subscription.platform.auth.service.AuthService;
import com.subscription.platform.common.dto.ApiResponse;
import com.subscription.platform.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Authentication & User Identity", description = "Global user registration, login, and profile access")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    @Operation(summary = "Register a new global user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        AuthResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", response));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate user and receive a JWT session token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @GetMapping("/users/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "alex@example.com";
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

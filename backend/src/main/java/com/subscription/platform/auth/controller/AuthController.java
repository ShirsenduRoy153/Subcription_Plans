package com.subscription.platform.auth.controller;

import com.subscription.platform.auth.dto.*;
import com.subscription.platform.auth.service.AuthService;
import com.subscription.platform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Business registration, login, OTP verification, and JWT issuance")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-business")
    @Operation(summary = "Register a new business (tenant) and initial admin user")
    public ResponseEntity<ApiResponse<AuthResponse>> registerBusiness(
            @Valid @RequestBody RegisterBusinessRequest request
    ) {
        AuthResponse response = authService.registerBusiness(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Business registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with password and receive a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Request a 6-digit OTP to login. Checks if email exists, applies 60s cooldown, and dispatches email.")
    public ResponseEntity<ApiResponse<OtpSendResponse>> sendOtp(
            @Valid @RequestBody OtpSendRequest request
    ) {
        OtpSendResponse response = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP code from email and receive a secure JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request
    ) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Verification successful. Logged in.", response));
    }
}

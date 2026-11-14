package com.subscription.platform.business.controller;

import com.subscription.platform.business.dto.BusinessResponse;
import com.subscription.platform.business.service.BusinessService;
import com.subscription.platform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Tag(name = "Business Management", description = "Operations on tenant business profiles")
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get currently authenticated business profile (requires Bearer token)")
    public ResponseEntity<ApiResponse<BusinessResponse>> getCurrentBusiness() {
        BusinessResponse response = businessService.getCurrentBusiness();
        return ResponseEntity.ok(ApiResponse.ok("Business profile retrieved successfully", response));
    }
}


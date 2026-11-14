package com.subscription.platform.feature.controller;

import com.subscription.platform.common.dto.ApiResponse;
import com.subscription.platform.feature.dto.CreateFeatureRequest;
import com.subscription.platform.feature.dto.FeatureResponse;
import com.subscription.platform.feature.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/me/features")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUSINESS_ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Feature Management", description = "Dynamic, industry-agnostic feature management for business plans")
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping
    @Operation(summary = "Create a custom feature (BOOLEAN, NUMBER, TEXT, LIST)")
    public ResponseEntity<ApiResponse<FeatureResponse>> createFeature(
            @Valid @RequestBody CreateFeatureRequest request
    ) {
        FeatureResponse response = featureService.createFeature(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feature created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all features defined by the business")
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getFeatures(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly
    ) {
        List<FeatureResponse> response = activeOnly
                ? featureService.getActiveBusinessFeatures()
                : featureService.getBusinessFeatures();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{featureId}/status")
    @Operation(summary = "Activate or deactivate a feature")
    public ResponseEntity<ApiResponse<FeatureResponse>> toggleFeatureStatus(
            @PathVariable Long featureId,
            @RequestParam boolean active
    ) {
        FeatureResponse response = featureService.toggleFeatureStatus(featureId, active);
        return ResponseEntity.ok(ApiResponse.ok("Feature status updated successfully", response));
    }
}


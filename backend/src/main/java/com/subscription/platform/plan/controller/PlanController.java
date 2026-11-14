package com.subscription.platform.plan.controller;

import com.subscription.platform.common.dto.ApiResponse;
import com.subscription.platform.plan.dto.CreatePlanRequest;
import com.subscription.platform.plan.dto.PlanResponse;
import com.subscription.platform.plan.service.PlanService;
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
@RequestMapping("/api/businesses/me/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUSINESS_ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Plan Management", description = "Create and manage priced subscription plans with custom dynamic feature values")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @Operation(summary = "Create a subscription plan with assigned feature values")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request
    ) {
        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Subscription plan created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all subscription plans for the business")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        List<PlanResponse> response = planService.getBusinessPlans();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get detailed plan by ID including its feature values")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlanDetails(@PathVariable Long planId) {
        PlanResponse response = planService.getPlanDetails(planId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{planId}/status")
    @Operation(summary = "Publish or unpublish a subscription plan")
    public ResponseEntity<ApiResponse<PlanResponse>> togglePlanStatus(
            @PathVariable Long planId,
            @RequestParam boolean active
    ) {
        PlanResponse response = planService.togglePlanStatus(planId, active);
        return ResponseEntity.ok(ApiResponse.ok("Plan status updated successfully", response));
    }
}


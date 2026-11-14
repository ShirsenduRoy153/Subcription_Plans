package com.subscription.platform.plan.controller;

import com.subscription.platform.common.dto.ApiResponse;
import com.subscription.platform.plan.dto.PlanResponse;
import com.subscription.platform.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/businesses/{slug}/plans")
@RequiredArgsConstructor
@Tag(name = "Public Storefront", description = "Publicly accessible storefront endpoints (cached with Redis for high performance)")
public class PublicPlanController {

    private final PlanService planService;

    @GetMapping
    @Operation(summary = "Get active plans for a business storefront by slug (Redis cached)")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPublicPlans(@PathVariable String slug) {
        List<PlanResponse> plans = planService.getPublicPlans(slug);
        return ResponseEntity.ok(ApiResponse.ok("Public plans retrieved successfully", plans));
    }
}


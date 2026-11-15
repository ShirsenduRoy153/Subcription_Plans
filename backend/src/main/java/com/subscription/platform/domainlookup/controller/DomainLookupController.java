package com.subscription.platform.domainlookup.controller;

import com.subscription.platform.common.dto.ApiResponse;
import com.subscription.platform.domainlookup.dto.DomainLookupResponse;
import com.subscription.platform.domainlookup.service.DomainLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/domains")
@RequiredArgsConstructor
@Tag(name = "Domain Lookups", description = "Public API to retrieve system reference choices (Business Types, Currencies, Roles)")
public class DomainLookupController {

    private final DomainLookupService domainLookupService;

    @GetMapping
    @Operation(summary = "Get active reference choices by domain type (e.g. ?type=BUSINESS_TYPE or ?type=CURRENCY)")
    public ResponseEntity<ApiResponse<List<DomainLookupResponse>>> getDomainsByType(
            @RequestParam(name = "type") String domainType
    ) {
        List<DomainLookupResponse> lookups = domainLookupService.getLookupsByType(domainType);
        return ResponseEntity.ok(ApiResponse.ok(lookups));
    }
}


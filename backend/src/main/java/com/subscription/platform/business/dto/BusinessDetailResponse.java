package com.subscription.platform.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDetailResponse {
    private Long id;
    private String businessName;
    private String businessSlug;
    private Long businessTypeId;
    private String businessType;
    private Long currencyId;
    private String currency;
    private String email;
    private String phone;
    private String address;
    private String timezone;
    private String logoUrl;
    private String coverImageUrl;
    private String primaryColor;
    private String tagline;
    private String status;
    private String userRole;
    private Integer hierarchyLevel;
    private Instant createdAt;
    private Instant updatedAt;
}


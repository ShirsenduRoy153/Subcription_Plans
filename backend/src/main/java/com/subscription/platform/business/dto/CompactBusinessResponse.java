package com.subscription.platform.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompactBusinessResponse {
    private Long id;
    private String businessName;
    private String businessSlug;
    private String businessType;
    private String currency;
    private String userRole;
    private Integer hierarchyLevel;
    private String status;
}


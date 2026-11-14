package com.subscription.platform.feature.dto;

import com.subscription.platform.feature.entity.FeatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResponse {
    private Long id;
    private Long businessId;
    private String featureName;
    private String featureCode;
    private String description;
    private FeatureType featureType;
    private boolean active;
    private Instant createdAt;
}


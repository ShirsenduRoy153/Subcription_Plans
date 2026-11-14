package com.subscription.platform.plan.dto;

import com.subscription.platform.feature.entity.FeatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFeatureResponse implements Serializable {
    private Long featureId;
    private String featureCode;
    private String featureName;
    private FeatureType featureType;
    private Boolean valueBoolean;
    private BigDecimal valueNumber;
    private String valueText;
    private List<String> valueJson;
}


package com.subscription.platform.plan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFeatureValueRequest {

    @NotNull(message = "Feature ID is required")
    private Long featureId;

    private Boolean valueBoolean;
    private BigDecimal valueNumber;
    private String valueText;
    private List<String> valueJson;
}


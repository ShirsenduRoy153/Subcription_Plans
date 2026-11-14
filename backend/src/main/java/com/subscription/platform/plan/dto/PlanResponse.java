package com.subscription.platform.plan.dto;

import com.subscription.platform.plan.entity.BillingInterval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse implements Serializable {
    private Long id;
    private Long businessId;
    private String planName;
    private String planCode;
    private String description;
    private BigDecimal price;
    private String currency;
    private BillingInterval billingInterval;
    private Integer billingIntervalCount;
    private Integer trialDays;
    private boolean active;
    private List<PlanFeatureResponse> features;
    private Instant createdAt;
}


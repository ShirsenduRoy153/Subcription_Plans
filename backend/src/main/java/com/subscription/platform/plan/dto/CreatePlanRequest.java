package com.subscription.platform.plan.dto;

import com.subscription.platform.plan.entity.BillingInterval;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreatePlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    private String planName;

    private String planCode;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    private String currency;

    @NotNull(message = "Billing interval is required")
    private BillingInterval billingInterval;

    @Builder.Default
    @Min(value = 1, message = "Billing interval count must be at least 1")
    private Integer billingIntervalCount = 1;

    @Builder.Default
    @Min(value = 0, message = "Trial days cannot be negative")
    private Integer trialDays = 0;

    @Valid
    private List<PlanFeatureValueRequest> features;
}


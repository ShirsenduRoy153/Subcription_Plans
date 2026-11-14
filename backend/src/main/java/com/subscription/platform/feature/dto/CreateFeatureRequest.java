package com.subscription.platform.feature.dto;

import com.subscription.platform.feature.entity.FeatureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeatureRequest {

    @NotBlank(message = "Feature name is required")
    @Size(max = 100, message = "Feature name cannot exceed 100 characters")
    private String featureName;

    @NotBlank(message = "Feature code is required")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Feature code must contain only lowercase alphanumeric characters and underscores")
    @Size(max = 100, message = "Feature code cannot exceed 100 characters")
    private String featureCode;

    private String description;

    @NotNull(message = "Feature type is required")
    private FeatureType featureType;
}


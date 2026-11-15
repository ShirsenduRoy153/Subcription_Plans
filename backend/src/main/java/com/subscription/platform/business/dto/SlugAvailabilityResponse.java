package com.subscription.platform.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlugAvailabilityResponse {
    private String slug;
    private boolean available;
    private String message;
}

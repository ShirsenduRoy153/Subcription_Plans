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
public class BusinessResponse {
    private Long id;
    private String businessName;
    private String businessSlug;
    private String businessType;
    private String email;
    private String phone;
    private String currency;
    private String timezone;
    private String status;
    private Instant createdAt;
}

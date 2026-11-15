package com.subscription.platform.business.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessRequest {

    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    private String businessName;

    private String phone;
    private String address;
    private String timezone;
    private String logoUrl;
    private String coverImageUrl;
    private String primaryColor;
    private String tagline;
    private String razorpayKeyId;
    private String razorpayKeySecret;
}


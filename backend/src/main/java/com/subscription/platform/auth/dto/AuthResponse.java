package com.subscription.platform.auth.dto;

import com.subscription.platform.business.dto.BusinessResponse;
import com.subscription.platform.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserResponse user;
    private BusinessResponse business;
}

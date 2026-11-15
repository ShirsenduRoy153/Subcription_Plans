package com.subscription.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendResponse {

    private boolean exists;
    private String message;
    private Long cooldownSeconds;
}


package com.subscription.platform.user.dto;

import com.subscription.platform.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private Long businessId;
    private String fullName;
    private String email;
    private Role role;
    private String status;
    private boolean emailVerified;
    private Instant createdAt;
}

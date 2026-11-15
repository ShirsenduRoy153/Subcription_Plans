package com.subscription.platform.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long membershipId;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String roleCode;
    private String roleName;
    private Integer hierarchyLevel;
    private List<String> permissions;
    private String status;
    private Instant joinedAt;
}


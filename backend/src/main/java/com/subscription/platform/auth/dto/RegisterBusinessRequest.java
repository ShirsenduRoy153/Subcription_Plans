package com.subscription.platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class RegisterBusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    private String businessName;

    @NotBlank(message = "Business slug is required")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain only lowercase alphanumeric characters and hyphens")
    @Size(max = 120, message = "Slug cannot exceed 120 characters")
    private String businessSlug;

    private String businessType;

    @NotBlank(message = "Owner name is required")
    @Size(max = 150, message = "Owner name cannot exceed 150 characters")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    private String currency;
}


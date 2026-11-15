package com.subscription.platform.business.dto;

import jakarta.validation.constraints.Email;
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
public class CreateBusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    private String businessName;

    @NotBlank(message = "Business slug is required")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain only lowercase alphanumeric characters and hyphens")
    @Size(max = 120, message = "Slug cannot exceed 120 characters")
    private String businessSlug;

    @NotNull(message = "Business type ID is required")
    private Long businessTypeId;

    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phone;
    private String address;
    private String timezone;
    private String tagline;
}


package com.subscription.platform.domainlookup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainLookupResponse {
    private Long id;
    private String domainType;
    private String code;
    private String value;
    private String description;
    private Integer sortOrder;
}


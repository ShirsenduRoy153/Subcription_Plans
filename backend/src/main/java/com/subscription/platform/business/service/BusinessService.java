package com.subscription.platform.business.service;

import com.subscription.platform.business.dto.BusinessResponse;
import com.subscription.platform.business.entity.Business;
import com.subscription.platform.business.repository.BusinessRepository;
import com.subscription.platform.common.exception.ResourceNotFoundException;
import com.subscription.platform.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;

    @Transactional(readOnly = true)
    public BusinessResponse getCurrentBusiness() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No active tenant context found for authenticated request");
        }

        Business business = businessRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found for ID: " + tenantId));

        return BusinessResponse.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .businessSlug(business.getBusinessSlug())
                .businessType(business.getBusinessType())
                .email(business.getEmail())
                .phone(business.getPhone())
                .currency(business.getCurrency())
                .timezone(business.getTimezone())
                .status(business.getStatus())
                .createdAt(business.getCreatedAt())
                .build();
    }
}


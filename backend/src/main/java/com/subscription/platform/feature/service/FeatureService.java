package com.subscription.platform.feature.service;

import com.subscription.platform.business.entity.Business;
import com.subscription.platform.business.repository.BusinessRepository;
import com.subscription.platform.common.exception.ConflictException;
import com.subscription.platform.common.exception.ResourceNotFoundException;
import com.subscription.platform.feature.dto.CreateFeatureRequest;
import com.subscription.platform.feature.dto.FeatureResponse;
import com.subscription.platform.feature.entity.BusinessFeature;
import com.subscription.platform.feature.repository.BusinessFeatureRepository;
import com.subscription.platform.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final BusinessFeatureRepository featureRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public FeatureResponse createFeature(CreateFeatureRequest request) {
        Long tenantId = getRequiredTenantId();
        String normalizedCode = request.getFeatureCode().toLowerCase().trim();

        log.info("Creating feature '{}' with code '{}' for tenant ID: {}", request.getFeatureName(), normalizedCode, tenantId);

        if (featureRepository.existsByBusinessIdAndFeatureCode(tenantId, normalizedCode)) {
            throw new ConflictException("Feature code '" + normalizedCode + "' already exists for your business");
        }

        BusinessFeature feature = BusinessFeature.builder()
                .businessId(tenantId)
                .featureName(request.getFeatureName().trim())
                .featureCode(normalizedCode)
                .description(request.getDescription())
                .featureType(request.getFeatureType())
                .active(true)
                .build();

        BusinessFeature saved = featureRepository.save(feature);
        evictPublicCacheForTenant(tenantId);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getBusinessFeatures() {
        Long tenantId = getRequiredTenantId();
        return featureRepository.findByBusinessId(tenantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getActiveBusinessFeatures() {
        Long tenantId = getRequiredTenantId();
        return featureRepository.findByBusinessIdAndActiveTrue(tenantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public FeatureResponse toggleFeatureStatus(Long featureId, boolean active) {
        Long tenantId = getRequiredTenantId();
        BusinessFeature feature = featureRepository.findByBusinessIdAndId(tenantId, featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + featureId));

        feature.setActive(active);
        BusinessFeature saved = featureRepository.save(feature);
        evictPublicCacheForTenant(tenantId);

        log.info("Toggled feature ID: {} status to active: {}", featureId, active);
        return mapToResponse(saved);
    }

    private Long getRequiredTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No active tenant context found for authenticated request");
        }
        return tenantId;
    }

    private void evictPublicCacheForTenant(Long tenantId) {
        businessRepository.findById(tenantId).ifPresent(business -> {
            evictPublicPlansCache(business.getBusinessSlug());
        });
    }

    @CacheEvict(value = "public-plans", key = "#slug")
    public void evictPublicPlansCache(String slug) {
        log.info("Evicted public plans cache for slug: {}", slug);
    }

    public FeatureResponse mapToResponse(BusinessFeature feature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .businessId(feature.getBusinessId())
                .featureName(feature.getFeatureName())
                .featureCode(feature.getFeatureCode())
                .description(feature.getDescription())
                .featureType(feature.getFeatureType())
                .active(feature.isActive())
                .createdAt(feature.getCreatedAt())
                .build();
    }
}


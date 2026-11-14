package com.subscription.platform.plan.service;

import com.subscription.platform.business.entity.Business;
import com.subscription.platform.business.repository.BusinessRepository;
import com.subscription.platform.common.exception.BadRequestException;
import com.subscription.platform.common.exception.ConflictException;
import com.subscription.platform.common.exception.ResourceNotFoundException;
import com.subscription.platform.feature.entity.BusinessFeature;
import com.subscription.platform.feature.entity.FeatureType;
import com.subscription.platform.feature.repository.BusinessFeatureRepository;
import com.subscription.platform.plan.dto.*;
import com.subscription.platform.plan.entity.Plan;
import com.subscription.platform.plan.entity.PlanFeature;
import com.subscription.platform.plan.repository.PlanRepository;
import com.subscription.platform.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;
    private final BusinessRepository businessRepository;
    private final BusinessFeatureRepository featureRepository;

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        Long tenantId = getRequiredTenantId();

        log.info("Creating plan '{}' for tenant ID: {}", request.getPlanName(), tenantId);

        if (planRepository.existsByBusinessIdAndPlanName(tenantId, request.getPlanName().trim())) {
            throw new ConflictException("A plan with name '" + request.getPlanName() + "' already exists for your business");
        }

        Business business = businessRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found for ID: " + tenantId));

        String currency = request.getCurrency() != null ? request.getCurrency().toUpperCase().trim() : business.getCurrency();

        Plan plan = Plan.builder()
                .businessId(tenantId)
                .planName(request.getPlanName().trim())
                .planCode(request.getPlanCode() != null ? request.getPlanCode().trim() : null)
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(currency)
                .billingInterval(request.getBillingInterval())
                .billingIntervalCount(request.getBillingIntervalCount() != null ? request.getBillingIntervalCount() : 1)
                .trialDays(request.getTrialDays() != null ? request.getTrialDays() : 0)
                .active(true)
                .planFeatures(new ArrayList<>())
                .build();

        // Validate and attach features
        if (request.getFeatures() != null && !request.getFeatures().isEmpty()) {
            Set<Long> featureIds = request.getFeatures().stream()
                    .map(PlanFeatureValueRequest::getFeatureId)
                    .collect(Collectors.toSet());

            List<BusinessFeature> existingFeatures = featureRepository.findAllByBusinessIdAndIdIn(tenantId, featureIds);
            Map<Long, BusinessFeature> featureMap = existingFeatures.stream()
                    .collect(Collectors.toMap(BusinessFeature::getId, Function.identity()));

            if (featureMap.size() != featureIds.size()) {
                throw new BadRequestException("One or more referenced features do not exist or do not belong to your business");
            }

            for (PlanFeatureValueRequest fVal : request.getFeatures()) {
                BusinessFeature feature = featureMap.get(fVal.getFeatureId());
                validateFeatureValue(feature, fVal);

                PlanFeature planFeature = PlanFeature.builder()
                        .plan(plan)
                        .feature(feature)
                        .valueBoolean(feature.getFeatureType() == FeatureType.BOOLEAN ? fVal.getValueBoolean() : null)
                        .valueNumber(feature.getFeatureType() == FeatureType.NUMBER ? fVal.getValueNumber() : null)
                        .valueText(feature.getFeatureType() == FeatureType.TEXT ? fVal.getValueText() : null)
                        .valueJson(feature.getFeatureType() == FeatureType.LIST ? fVal.getValueJson() : null)
                        .build();

                plan.addPlanFeature(planFeature);
            }
        }

        Plan saved = planRepository.save(plan);
        evictPublicCache(business.getBusinessSlug());

        log.info("Plan created successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getBusinessPlans() {
        Long tenantId = getRequiredTenantId();
        return planRepository.findByBusinessId(tenantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanDetails(Long planId) {
        Long tenantId = getRequiredTenantId();
        Plan plan = planRepository.findPlanWithFeatures(planId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));
        return mapToResponse(plan);
    }

    @Transactional
    public PlanResponse togglePlanStatus(Long planId, boolean active) {
        Long tenantId = getRequiredTenantId();
        Plan plan = planRepository.findByBusinessIdAndId(tenantId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        plan.setActive(active);
        Plan saved = planRepository.save(plan);

        businessRepository.findById(tenantId).ifPresent(b -> evictPublicCache(b.getBusinessSlug()));

        log.info("Toggled plan ID: {} active status to: {}", planId, active);
        return mapToResponse(saved);
    }

    @Cacheable(value = "public-plans", key = "#slug")
    @Transactional(readOnly = true)
    public List<PlanResponse> getPublicPlans(String slug) {
        log.info("Cache miss for public plans of slug: '{}' - querying database", slug);

        Business business = businessRepository.findByBusinessSlug(slug.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with slug: " + slug));

        List<Plan> activePlans = planRepository.findActivePlansWithFeaturesByBusinessId(business.getId());
        return activePlans.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @CacheEvict(value = "public-plans", key = "#slug")
    public void evictPublicCache(String slug) {
        log.info("Evicted public plans cache for slug: '{}'", slug);
    }

    private void validateFeatureValue(BusinessFeature feature, PlanFeatureValueRequest request) {
        switch (feature.getFeatureType()) {
            case BOOLEAN -> {
                if (request.getValueBoolean() == null) {
                    throw new BadRequestException("Feature '" + feature.getFeatureName() + "' requires a boolean value");
                }
            }
            case NUMBER -> {
                if (request.getValueNumber() == null) {
                    throw new BadRequestException("Feature '" + feature.getFeatureName() + "' requires a numeric value");
                }
            }
            case TEXT -> {
                if (request.getValueText() == null || request.getValueText().isBlank()) {
                    throw new BadRequestException("Feature '" + feature.getFeatureName() + "' requires a non-empty text value");
                }
            }
            case LIST -> {
                if (request.getValueJson() == null || request.getValueJson().isEmpty()) {
                    throw new BadRequestException("Feature '" + feature.getFeatureName() + "' requires a non-empty list of values");
                }
            }
        }
    }

    private Long getRequiredTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No active tenant context found for authenticated request");
        }
        return tenantId;
    }

    public PlanResponse mapToResponse(Plan plan) {
        List<PlanFeatureResponse> featureResponses = new ArrayList<>();
        if (plan.getPlanFeatures() != null) {
            for (PlanFeature pf : plan.getPlanFeatures()) {
                BusinessFeature f = pf.getFeature();
                featureResponses.add(PlanFeatureResponse.builder()
                        .featureId(f.getId())
                        .featureCode(f.getFeatureCode())
                        .featureName(f.getFeatureName())
                        .featureType(f.getFeatureType())
                        .valueBoolean(pf.getValueBoolean())
                        .valueNumber(pf.getValueNumber())
                        .valueText(pf.getValueText())
                        .valueJson(pf.getValueJson())
                        .build());
            }
        }

        return PlanResponse.builder()
                .id(plan.getId())
                .businessId(plan.getBusinessId())
                .planName(plan.getPlanName())
                .planCode(plan.getPlanCode())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingInterval(plan.getBillingInterval())
                .billingIntervalCount(plan.getBillingIntervalCount())
                .trialDays(plan.getTrialDays())
                .active(plan.isActive())
                .features(featureResponses)
                .createdAt(plan.getCreatedAt())
                .build();
    }
}


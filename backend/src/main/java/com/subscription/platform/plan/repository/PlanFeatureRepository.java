package com.subscription.platform.plan.repository;

import com.subscription.platform.plan.entity.PlanFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanFeatureRepository extends JpaRepository<PlanFeature, Long> {

    List<PlanFeature> findByPlanId(Long planId);

    boolean existsByFeatureId(Long featureId);
}


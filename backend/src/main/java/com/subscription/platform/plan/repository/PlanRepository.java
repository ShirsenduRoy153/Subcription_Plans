package com.subscription.platform.plan.repository;

import com.subscription.platform.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByBusinessId(Long businessId);

    Optional<Plan> findByBusinessIdAndId(Long businessId, Long id);

    boolean existsByBusinessIdAndPlanName(Long businessId, String planName);

    @Query("SELECT DISTINCT p FROM Plan p " +
           "LEFT JOIN FETCH p.planFeatures pf " +
           "LEFT JOIN FETCH pf.feature " +
           "WHERE p.businessId = :businessId AND p.active = true")
    List<Plan> findActivePlansWithFeaturesByBusinessId(@Param("businessId") Long businessId);

    @Query("SELECT DISTINCT p FROM Plan p " +
           "LEFT JOIN FETCH p.planFeatures pf " +
           "LEFT JOIN FETCH pf.feature " +
           "WHERE p.id = :id AND p.businessId = :businessId")
    Optional<Plan> findPlanWithFeatures(@Param("id") Long id, @Param("businessId") Long businessId);
}


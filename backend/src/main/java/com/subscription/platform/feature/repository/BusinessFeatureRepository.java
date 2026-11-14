package com.subscription.platform.feature.repository;

import com.subscription.platform.feature.entity.BusinessFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessFeatureRepository extends JpaRepository<BusinessFeature, Long> {

    List<BusinessFeature> findByBusinessId(Long businessId);

    List<BusinessFeature> findByBusinessIdAndActiveTrue(Long businessId);

    Optional<BusinessFeature> findByBusinessIdAndId(Long businessId, Long id);

    boolean existsByBusinessIdAndFeatureCode(Long businessId, String featureCode);

    List<BusinessFeature> findAllByBusinessIdAndIdIn(Long businessId, Collection<Long> ids);
}


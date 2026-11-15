package com.subscription.platform.business.repository;

import com.subscription.platform.business.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByBusinessSlug(String businessSlug);

    boolean existsByBusinessSlug(String businessSlug);
}

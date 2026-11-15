package com.subscription.platform.domainlookup.repository;

import com.subscription.platform.domainlookup.entity.DomainLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainLookupRepository extends JpaRepository<DomainLookup, Long> {

    List<DomainLookup> findByDomainTypeAndActiveTrueOrderBySortOrderAsc(String domainType);

    Optional<DomainLookup> findByDomainTypeAndLookupCode(String domainType, String lookupCode);

    boolean existsByDomainTypeAndLookupCode(String domainType, String lookupCode);
}


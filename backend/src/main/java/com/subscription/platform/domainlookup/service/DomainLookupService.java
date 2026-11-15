package com.subscription.platform.domainlookup.service;

import com.subscription.platform.domainlookup.dto.DomainLookupResponse;
import com.subscription.platform.domainlookup.entity.DomainLookup;
import com.subscription.platform.domainlookup.repository.DomainLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainLookupService {

    private final DomainLookupRepository domainLookupRepository;

    @Transactional(readOnly = true)
    public List<DomainLookupResponse> getLookupsByType(String domainType) {
        String normalizedType = domainType.toUpperCase().trim();
        log.info("Fetching domain lookups for type: {}", normalizedType);

        return domainLookupRepository.findByDomainTypeAndActiveTrueOrderBySortOrderAsc(normalizedType)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DomainLookupResponse mapToResponse(DomainLookup lookup) {
        return DomainLookupResponse.builder()
                .id(lookup.getId())
                .domainType(lookup.getDomainType())
                .code(lookup.getLookupCode())
                .value(lookup.getLookupValue())
                .description(lookup.getDescription())
                .sortOrder(lookup.getSortOrder())
                .build();
    }
}

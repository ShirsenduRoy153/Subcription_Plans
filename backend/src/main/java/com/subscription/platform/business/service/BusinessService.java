package com.subscription.platform.business.service;

import com.subscription.platform.business.dto.*;
import com.subscription.platform.business.entity.Business;
import com.subscription.platform.business.entity.BusinessMember;
import com.subscription.platform.business.repository.BusinessMemberRepository;
import com.subscription.platform.business.repository.BusinessRepository;
import com.subscription.platform.common.exception.BadRequestException;
import com.subscription.platform.common.exception.ConflictException;
import com.subscription.platform.common.exception.ResourceNotFoundException;
import com.subscription.platform.domainlookup.entity.DomainLookup;
import com.subscription.platform.domainlookup.repository.DomainLookupRepository;
import com.subscription.platform.role.entity.Role;
import com.subscription.platform.role.repository.RoleRepository;
import com.subscription.platform.user.entity.User;
import com.subscription.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final DomainLookupRepository domainLookupRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SlugAvailabilityResponse checkSlugAvailability(String rawSlug) {
        String slug = rawSlug.toLowerCase().trim();
        boolean exists = businessRepository.existsByBusinessSlug(slug);

        return SlugAvailabilityResponse.builder()
                .slug(slug)
                .available(!exists)
                .message(!exists ? "Slug is available" : "Slug is already in use by another business")
                .build();
    }

    @Transactional
    public BusinessDetailResponse createBusiness(CreateBusinessRequest request, Long userId) {
        String slug = request.getBusinessSlug().toLowerCase().trim();
        log.info("Creating new business workspace '{}' (slug: {}) by user ID: {}", request.getBusinessName(), slug, userId);

        if (businessRepository.existsByBusinessSlug(slug)) {
            throw new ConflictException("Business slug '" + slug + "' is already registered");
        }

        DomainLookup businessType = domainLookupRepository.findById(request.getBusinessTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Business type not found with ID: " + request.getBusinessTypeId()));

        DomainLookup currency = domainLookupRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with ID: " + request.getCurrencyId()));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // 1. Create Business
        Business business = Business.builder()
                .businessName(request.getBusinessName().trim())
                .businessSlug(slug)
                .businessType(businessType)
                .currency(currency)
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .address(request.getAddress())
                .timezone(request.getTimezone() != null && !request.getTimezone().isBlank() ? request.getTimezone() : "Asia/Kolkata")
                .tagline(request.getTagline())
                .status("ACTIVE")
                .build();

        Business savedBusiness = businessRepository.save(business);

        // 2. Fetch Global OWNER Role
        Role ownerRole = roleRepository.findByRoleCodeAndBusinessIdIsNull("OWNER")
                .orElseThrow(() -> new ResourceNotFoundException("System default OWNER role not found"));

        // 3. Link Creator as OWNER in business_members
        BusinessMember membership = BusinessMember.builder()
                .business(savedBusiness)
                .user(creator)
                .role(ownerRole)
                .status("ACTIVE")
                .build();

        businessMemberRepository.save(membership);
        log.info("User ID {} linked as OWNER of business ID {}", userId, savedBusiness.getId());

        return mapToDetailResponse(savedBusiness, ownerRole.getRoleCode(), ownerRole.getHierarchyLevel());
    }

    @Transactional(readOnly = true)
    public List<CompactBusinessResponse> getUserWorkspaces(Long userId) {
        log.info("Fetching all workspaces for user ID: {}", userId);

        List<BusinessMember> memberships = businessMemberRepository.findByUserIdWithDetails(userId);

        return memberships.stream()
                .map(bm -> CompactBusinessResponse.builder()
                        .id(bm.getBusiness().getId())
                        .businessName(bm.getBusiness().getBusinessName())
                        .businessSlug(bm.getBusiness().getBusinessSlug())
                        .businessType(bm.getBusiness().getBusinessType().getLookupValue())
                        .currency(bm.getBusiness().getCurrency().getLookupCode())
                        .userRole(bm.getRole().getRoleCode())
                        .hierarchyLevel(bm.getRole().getHierarchyLevel())
                        .status(bm.getBusiness().getStatus())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessDetailResponse getBusinessDetails(String idOrSlug, Long currentUserId) {
        Business business = resolveBusiness(idOrSlug);

        String userRole = null;
        Integer hierarchyLevel = null;

        if (currentUserId != null) {
            Optional<BusinessMember> memberOpt = businessMemberRepository.findByBusinessIdAndUserId(business.getId(), currentUserId);
            if (memberOpt.isPresent()) {
                userRole = memberOpt.get().getRole().getRoleCode();
                hierarchyLevel = memberOpt.get().getRole().getHierarchyLevel();
            }
        }

        return mapToDetailResponse(business, userRole, hierarchyLevel);
    }

    @Transactional
    public BusinessDetailResponse updateBusiness(String idOrSlug, UpdateBusinessRequest request, Long currentUserId) {
        Business business = resolveBusiness(idOrSlug);

        // Check that current user is an OWNER or ADMIN (hierarchy >= 80)
        BusinessMember member = businessMemberRepository.findByBusinessIdAndUserId(business.getId(), currentUserId)
                .orElseThrow(() -> new BadRequestException("You are not a member of this workspace"));

        if (member.getRole().getHierarchyLevel() < 80) {
            throw new BadRequestException("Only an OWNER or ADMINISTRATOR can update business settings");
        }

        if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
            business.setBusinessName(request.getBusinessName().trim());
        }
        if (request.getPhone() != null) business.setPhone(request.getPhone().trim());
        if (request.getAddress() != null) business.setAddress(request.getAddress().trim());
        if (request.getTimezone() != null) business.setTimezone(request.getTimezone().trim());
        if (request.getLogoUrl() != null) business.setLogoUrl(request.getLogoUrl().trim());
        if (request.getCoverImageUrl() != null) business.setCoverImageUrl(request.getCoverImageUrl().trim());
        if (request.getPrimaryColor() != null) business.setPrimaryColor(request.getPrimaryColor().trim());
        if (request.getTagline() != null) business.setTagline(request.getTagline().trim());
        if (request.getRazorpayKeyId() != null) business.setRazorpayKeyId(request.getRazorpayKeyId().trim());
        if (request.getRazorpayKeySecret() != null) business.setRazorpayKeySecret(request.getRazorpayKeySecret().trim());

        Business updated = businessRepository.save(business);
        log.info("Business ID {} updated by user ID {}", updated.getId(), currentUserId);

        return mapToDetailResponse(updated, member.getRole().getRoleCode(), member.getRole().getHierarchyLevel());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getBusinessMembers(String idOrSlug, Long currentUserId) {
        Business business = resolveBusiness(idOrSlug);

        // Verify membership
        businessMemberRepository.findByBusinessIdAndUserId(business.getId(), currentUserId)
                .orElseThrow(() -> new BadRequestException("You do not have permission to view members of this workspace"));

        List<BusinessMember> members = businessMemberRepository.findByBusinessIdWithDetails(business.getId());

        return members.stream()
                .map(m -> MemberResponse.builder()
                        .membershipId(m.getId())
                        .userId(m.getUser().getId())
                        .fullName(m.getUser().getFullName())
                        .email(m.getUser().getEmail())
                        .phone(m.getUser().getPhone())
                        .roleCode(m.getRole().getRoleCode())
                        .roleName(m.getRole().getRoleName())
                        .hierarchyLevel(m.getRole().getHierarchyLevel())
                        .permissions(m.getRole().getPermissions())
                        .status(m.getStatus())
                        .joinedAt(m.getCreatedAt())
                        .build())
                .toList();
    }

    private Business resolveBusiness(String idOrSlug) {
        try {
            Long id = Long.parseLong(idOrSlug);
            return businessRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + id));
        } catch (NumberFormatException e) {
            return businessRepository.findByBusinessSlug(idOrSlug.toLowerCase().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Business not found with slug: " + idOrSlug));
        }
    }

    private BusinessDetailResponse mapToDetailResponse(Business b, String userRole, Integer hierarchyLevel) {
        return BusinessDetailResponse.builder()
                .id(b.getId())
                .businessName(b.getBusinessName())
                .businessSlug(b.getBusinessSlug())
                .businessTypeId(b.getBusinessType().getId())
                .businessType(b.getBusinessType().getLookupValue())
                .currencyId(b.getCurrency().getId())
                .currency(b.getCurrency().getLookupCode())
                .email(b.getEmail())
                .phone(b.getPhone())
                .address(b.getAddress())
                .timezone(b.getTimezone())
                .logoUrl(b.getLogoUrl())
                .coverImageUrl(b.getCoverImageUrl())
                .primaryColor(b.getPrimaryColor())
                .tagline(b.getTagline())
                .status(b.getStatus())
                .userRole(userRole)
                .hierarchyLevel(hierarchyLevel)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}

package com.subscription.platform.auth.service;

import com.subscription.platform.auth.dto.AuthResponse;
import com.subscription.platform.auth.dto.LoginRequest;
import com.subscription.platform.auth.dto.RegisterBusinessRequest;
import com.subscription.platform.business.dto.BusinessResponse;
import com.subscription.platform.business.entity.Business;
import com.subscription.platform.business.repository.BusinessRepository;
import com.subscription.platform.common.exception.ConflictException;
import com.subscription.platform.common.exception.ResourceNotFoundException;
import com.subscription.platform.security.JwtService;
import com.subscription.platform.user.dto.UserResponse;
import com.subscription.platform.user.entity.Role;
import com.subscription.platform.user.entity.User;
import com.subscription.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registerBusiness(RegisterBusinessRequest request) {
        log.info("Registering new business with slug: {}", request.getBusinessSlug());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email " + request.getEmail() + " is already registered");
        }

        if (businessRepository.existsByBusinessSlug(request.getBusinessSlug())) {
            throw new ConflictException("Business slug '" + request.getBusinessSlug() + "' is already taken");
        }

        // 1. Create Business
        Business business = Business.builder()
                .businessName(request.getBusinessName())
                .businessSlug(request.getBusinessSlug().toLowerCase().trim())
                .businessType(request.getBusinessType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR")
                .status("ACTIVE")
                .build();

        Business savedBusiness = businessRepository.save(business);

        // 2. Create Business Admin User
        User user = User.builder()
                .businessId(savedBusiness.getId())
                .fullName(request.getOwnerName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.BUSINESS_ADMIN)
                .status("ACTIVE")
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // 3. Generate JWT
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .business(mapToBusinessResponse(savedBusiness))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        Business business = null;
        if (user.getBusinessId() != null) {
            business = businessRepository.findById(user.getBusinessId()).orElse(null);
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .business(business != null ? mapToBusinessResponse(business) : null)
                .build();
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .businessId(user.getBusinessId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public BusinessResponse mapToBusinessResponse(Business business) {
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


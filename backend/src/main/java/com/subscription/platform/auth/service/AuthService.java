package com.subscription.platform.auth.service;

import com.subscription.platform.auth.dto.*;
import com.subscription.platform.business.dto.BusinessResponse;
import com.subscription.platform.business.entity.Business;
import com.subscription.platform.business.repository.BusinessRepository;
import com.subscription.platform.common.exception.ConflictException;
import com.subscription.platform.common.exception.ResourceNotFoundException;
import com.subscription.platform.notification.service.EmailService;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse registerBusiness(RegisterBusinessRequest request) {
        log.info("Registering new business with slug: {}", request.getBusinessSlug());

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        String normalizedSlug = request.getBusinessSlug().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email " + normalizedEmail + " is already registered");
        }

        if (businessRepository.existsByBusinessSlug(normalizedSlug)) {
            throw new ConflictException("Business slug '" + normalizedSlug + "' is already taken");
        }

        // 1. Create Business
        Business business = Business.builder()
                .businessName(request.getBusinessName())
                .businessSlug(normalizedSlug)
                .businessType(request.getBusinessType())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR")
                .status("ACTIVE")
                .build();

        Business savedBusiness = businessRepository.save(business);

        // 2. Create Business Admin User
        User user = User.builder()
                .businessId(savedBusiness.getId())
                .fullName(request.getOwnerName())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.BUSINESS_ADMIN)
                .status("ACTIVE")
                .emailVerified(true)
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
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        log.info("Authenticating user with email: {}", normalizedEmail);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

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

    /**
     * Checks if email exists. If found, generates OTP, enforces cooldown, and emails the code.
     * If not found, informs frontend that the user does not exist so they can redirect to sign up.
     */
    public OtpSendResponse sendOtp(OtpSendRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        log.info("Processing OTP request for email: {}", normalizedEmail);

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            log.info("No user found for email: {}. Prompting sign up.", normalizedEmail);
            return OtpSendResponse.builder()
                    .exists(false)
                    .message("No account found with this email. Please sign up to create your business account.")
                    .cooldownSeconds(0L)
                    .build();
        }

        // Check if rate-limited cooldown is active
        long remainingCooldown = otpService.getRemainingCooldown(normalizedEmail);
        if (remainingCooldown > 0) {
            return OtpSendResponse.builder()
                    .exists(true)
                    .message("A verification code was already requested. Please wait before requesting another.")
                    .cooldownSeconds(remainingCooldown)
                    .build();
        }

        User user = userOptional.get();
        String otp = otpService.generateAndStoreOtp(normalizedEmail);

        // Send email via Mailpit / SMTP
        emailService.sendOtpEmail(normalizedEmail, user.getFullName(), otp);

        return OtpSendResponse.builder()
                .exists(true)
                .message("A 6-digit verification code has been sent to your email.")
                .cooldownSeconds(60L)
                .build();
    }

    /**
     * Verifies OTP against Redis. On success, issues JWT token and returns user + business info.
     */
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        log.info("Verifying OTP for email: {}", normalizedEmail);

        // 1. Verify with Redis (will throw BadRequestException if expired, invalid, or exceeded)
        otpService.verifyOtp(normalizedEmail, request.getOtp());

        // 2. Load user
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        // 3. Mark email as verified if not already
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        // 4. Fetch business profile
        Business business = null;
        if (user.getBusinessId() != null) {
            business = businessRepository.findById(user.getBusinessId()).orElse(null);
        }

        // 5. Generate JWT token
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

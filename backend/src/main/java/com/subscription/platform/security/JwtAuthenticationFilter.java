package com.subscription.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String tenantHeader = request.getHeader("X-Tenant-ID");

        // Dev Mode: If no JWT token is supplied, fallback to Dev Authentication with ROLE_BUSINESS_ADMIN
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Long devTenantId = 1L;
            if (tenantHeader != null && !tenantHeader.isBlank()) {
                try {
                    devTenantId = Long.parseLong(tenantHeader.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid X-Tenant-ID header: {}. Defaulting to tenant 1", tenantHeader);
                }
            }

            // Set Dev Authentication with ROLE_BUSINESS_ADMIN so @PreAuthorize passes
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_BUSINESS_ADMIN");
            UsernamePasswordAuthenticationToken devAuth = new UsernamePasswordAuthenticationToken(
                    "dev-admin@fitzone.com",
                    null,
                    List.of(authority)
            );
            devAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(devAuth);

            TenantContext.setTenantId(devTenantId);
            org.slf4j.MDC.put("tenantId", String.valueOf(devTenantId));

            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
            }
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtService.extractUsername(jwt);
            final Long businessId = jwtService.extractBusinessId(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    if (businessId != null) {
                        TenantContext.setTenantId(businessId);
                        org.slf4j.MDC.put("tenantId", String.valueOf(businessId));
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

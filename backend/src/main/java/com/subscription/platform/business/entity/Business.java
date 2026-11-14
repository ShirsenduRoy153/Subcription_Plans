package com.subscription.platform.business.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "business_slug", nullable = false, unique = true, length = 120)
    private String businessSlug;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 60)
    private String timezone = "Asia/Kolkata";

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}


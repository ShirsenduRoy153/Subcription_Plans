package com.subscription.platform.business.entity;

import com.subscription.platform.domainlookup.entity.DomainLookup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_type_id", nullable = false)
    private DomainLookup businessType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private DomainLookup currency;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 60)
    private String timezone = "Asia/Kolkata";

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Builder.Default
    @Column(name = "primary_color", length = 10)
    private String primaryColor = "#4F46E5";

    @Column(name = "tagline", length = 255)
    private String tagline;

    @Column(name = "razorpay_key_id", length = 100)
    private String razorpayKeyId;

    @Column(name = "razorpay_key_secret", length = 100)
    private String razorpayKeySecret;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

package com.subscription.platform.role.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uq_role_code_per_business", columnNames = {"business_id", "role_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    @Column(name = "hierarchy_level", nullable = false)
    @Builder.Default
    private Integer hierarchyLevel = 10;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> permissions = new ArrayList<>();

    @Column(name = "is_system_default", nullable = false)
    @Builder.Default
    private boolean systemDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

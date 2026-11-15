package com.subscription.platform.role.repository;

import com.subscription.platform.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleCodeAndBusinessIdIsNull(String roleCode);

    Optional<Role> findByRoleCodeAndBusinessId(String roleCode, Long businessId);

    List<Role> findByBusinessIdIsNull();

    List<Role> findByBusinessId(Long businessId);
}


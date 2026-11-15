package com.subscription.platform.business.repository;

import com.subscription.platform.business.entity.BusinessMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

    @Query("SELECT bm FROM BusinessMember bm JOIN FETCH bm.business b JOIN FETCH bm.role r WHERE bm.user.id = :userId")
    List<BusinessMember> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT bm FROM BusinessMember bm JOIN FETCH bm.user u JOIN FETCH bm.role r WHERE bm.business.id = :businessId")
    List<BusinessMember> findByBusinessIdWithDetails(@Param("businessId") Long businessId);

    Optional<BusinessMember> findByBusinessIdAndUserId(Long businessId, Long userId);

    boolean existsByBusinessIdAndUserId(Long businessId, Long userId);
}

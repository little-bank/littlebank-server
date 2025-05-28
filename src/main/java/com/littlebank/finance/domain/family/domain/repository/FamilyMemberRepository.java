package com.littlebank.finance.domain.family.domain.repository;

import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long>, CustomFamilyMemberRepository {
    boolean existsByUserIdAndStatus(Long userId, Status status);

    @Query(value = "SELECT * FROM family_member WHERE family = :familyId AND user = :userId", nativeQuery = true)
    Optional<FamilyMember> findByFamilyIdAndUserIdIncludingDeleted(@Param("familyId") Long familyId, @Param("userId") Long userId);
    List<FamilyMember> findAllByUserIdAndStatus(Long userId, Status status);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE family_member SET is_deleted = true WHERE user = :userId AND status = :status", nativeQuery = true)
    int deleteByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    Optional<FamilyMember> findByUserId(Long userId);

}

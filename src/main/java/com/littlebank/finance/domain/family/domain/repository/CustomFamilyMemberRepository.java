package com.littlebank.finance.domain.family.domain.repository;

import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import com.littlebank.finance.domain.family.dto.response.FamilyInfoResponse;

import java.util.List;
import java.util.Optional;

public interface CustomFamilyMemberRepository {
    Optional<FamilyMember> findByUserIdAndStatusWithFamily(Long userId, Status status);
    Optional<FamilyMember> findByUserIdAndStatusWithUser(Long userId, Status status);
    List<FamilyMember> findByMemberIdWithFamilyAndUser(Long memberId);
    List<FamilyMember> findAllByFamilyIdAndStatusWithUser(Long familyId, Status status);
    List<FamilyMember> findParentsByFamilyId(Long familyId);

    FamilyInfoResponse getFamilyInfoByUserId(Long userId);
    List<FamilyMember> findChildrenByParentUserId(Long id);
    String findChildNickNameByChildId(Long userId);
}

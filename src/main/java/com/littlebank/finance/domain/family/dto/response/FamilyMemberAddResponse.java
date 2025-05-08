package com.littlebank.finance.domain.family.dto.response;

import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilyMemberAddResponse {
    private Long familyMemberId;
    private Long familyId;
    private Status status;

    public static FamilyMemberAddResponse of(FamilyMember member) {
        return FamilyMemberAddResponse.builder()
                .familyMemberId(member.getId())
                .familyId(member.getFamily().getId())
                .status(member.getStatus())
                .build();
    }
}

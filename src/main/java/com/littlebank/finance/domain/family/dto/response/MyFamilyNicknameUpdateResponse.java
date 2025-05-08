package com.littlebank.finance.domain.family.dto.response;

import com.littlebank.finance.domain.family.domain.FamilyMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyFamilyNicknameUpdateResponse {
    private Long familyMemberId;
    private String changedNickname;

    public static MyFamilyNicknameUpdateResponse of(FamilyMember familyMember) {
        return MyFamilyNicknameUpdateResponse.builder()
                .familyMemberId(familyMember.getId())
                .changedNickname(familyMember.getNickname())
                .build();
    }
}

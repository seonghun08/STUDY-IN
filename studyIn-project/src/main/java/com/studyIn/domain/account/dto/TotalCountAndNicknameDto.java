package com.studyIn.domain.account.dto;

import lombok.Data;

@Data
public class TotalCountAndNicknameDto {

    /**
     * 전체 회원 수
     */
    private Long totalCount;
    private String nickname;

    public TotalCountAndNicknameDto(Long totalCount, String nickname) {
        this.totalCount = totalCount;
        this.nickname = nickname;
    }
}

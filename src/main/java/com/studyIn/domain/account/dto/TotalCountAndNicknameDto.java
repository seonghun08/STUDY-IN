package com.studyIn.domain.account.dto;

import lombok.Data;

@Data
public class TotalCountAndNicknameDto {

    private Long totalCount;
    private String nickname;

    public TotalCountAndNicknameDto(Long totalCount, String nickname) {
        this.totalCount = totalCount;
        this.nickname = nickname;
    }
}

package com.studyIn.domain.account.dto;

import lombok.Data;

@Data
public class AccountDTO {

    /**
     * 전체 계정 수
     */
    private Long totalCount;

    private String nickname;

    private String profileImage;


    public AccountDTO(Long totalCount, String nickname) {
        this.totalCount = totalCount;
        this.nickname = nickname;
    }
}

package com.studyIn.domain.account.dto.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class ProfileForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$", message = "한글, 영문, 숫자와 특수 기호(_),(-)만 사용 가능합니다.")
    private String nickname;

    @Size(max = 35)
    private String bio;

    @Size(max = 50)
    private String link;

    @Size(max = 30)
    private String occupation;

    @Size(max = 50)
    private String location;

    private String profileImage;
}

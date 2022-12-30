package com.studyIn.domain.account.form;

import com.studyIn.domain.account.Gender;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class SignUpForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 2, max = 20, message = "2~20자 이내로 입력해야 합니다.")
    @Pattern(regexp = "^[a-z0-9_-]{2,20}$", message = "영문 소문자, 숫자와 특수 기호(_),(-)만 사용 가능합니다.")
    private String username;

    @NotBlank(message = "필수 정보입니다.")
    @Email
    private String email;

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String password;

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 2, max = 20, message = "2~20자 이내로 입력해야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$", message = "한글, 영문, 숫자와 특수 기호(_),(-)만 사용 가능합니다.")
    private String nickname;

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 8, max = 11, message = "8~11자 이내로 입력해야 합니다.")
    @Pattern(regexp = "^[0-9]{8,11}${8,11}", message = "숫자만 입력할 수 있습니다.")
    private String cellPhone;

    private Gender gender;

    @NotBlank(message = "필수 정보입니다.")
    private String birthday;
}

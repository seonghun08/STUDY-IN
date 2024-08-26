package com.studyIn.domain.account.dto.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PasswordForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String currentPassword;

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String newPassword;

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String confirmPassword;
}

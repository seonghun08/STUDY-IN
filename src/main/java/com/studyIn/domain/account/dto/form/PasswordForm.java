package com.studyIn.domain.account.dto.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PasswordForm {

    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String currentPassword;

    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String newPassword;

    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해야 합니다.")
    private String confirmPassword;
}

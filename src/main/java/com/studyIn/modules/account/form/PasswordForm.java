package com.studyIn.modules.account.form;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class PasswordForm {

    @Size(min = 8, max = 50)
    private String currentPassword;

    @Size(min = 8, max = 50)
    private String newPassword;

    @Size(min = 8, max = 50)
    private String confirmPassword;
}

package com.studyIn.domain.account.dto.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailForm {

    @NotBlank(message = "필수 정보입니다.")
    @Email
    private String email;
}

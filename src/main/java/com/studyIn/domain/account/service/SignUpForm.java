package com.studyIn.domain.account.service;

import com.studyIn.domain.account.Gender;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class SignUpForm {

    @NotBlank @Size(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 50)
    private String password;

    @NotBlank
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String nickname;

    private String cellPhone;

    private Gender gender;

    private String birthday;

    private String city;

    private String street;

    private String zipcode;
}

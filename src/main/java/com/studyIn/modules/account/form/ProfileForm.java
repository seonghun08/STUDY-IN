package com.studyIn.modules.account.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class ProfileForm {

    @NotBlank
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String nickname;

    @Size(max = 35)
    private String bio;

    @Size(max = 50)
    private String url;

    @Size(max = 30)
    private String occupation;

    @Size(max = 50)
    private String address;

    @Size(max = 15)
    private String phone;

    private String birthday;

    private String profileImage;
}

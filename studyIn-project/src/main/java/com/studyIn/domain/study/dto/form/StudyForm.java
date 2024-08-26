package com.studyIn.domain.study.dto.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class StudyForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(min = 2, max = 20, message = "2~20자 이내로 입력해야 합니다.")
    @Pattern(regexp = "^[a-z0-9_-]{2,20}$", message = "영문 소문자, 숫자와 특수 기호(_),(-)만 사용 가능합니다.")
    private String path;

    @NotBlank(message = "필수 정보입니다.")
    @Size(max = 30)
    private String title;

    @NotBlank(message = "필수 정보입니다.")
    @Size(max = 100)
    private String shortDescription;

    private String fullDescription;
}

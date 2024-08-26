package com.studyIn.domain.study.dto.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TitleForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(max = 30)
    private String title;
}

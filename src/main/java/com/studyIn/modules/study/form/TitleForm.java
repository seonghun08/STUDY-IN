package com.studyIn.modules.study.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class TitleForm {

    @NotBlank
    @Length(max = 30)
    private String title;
}

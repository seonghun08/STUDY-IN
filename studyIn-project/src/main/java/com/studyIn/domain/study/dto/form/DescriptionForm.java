package com.studyIn.domain.study.dto.form;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class DescriptionForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(max = 100)
    private String shortDescription;

    private String fullDescription;
}
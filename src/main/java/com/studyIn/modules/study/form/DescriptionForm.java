package com.studyIn.modules.study.form;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class DescriptionForm {

    @NotBlank
    @Size(max = 100)
    private String shortDescription;

    private String fullDescription;
}

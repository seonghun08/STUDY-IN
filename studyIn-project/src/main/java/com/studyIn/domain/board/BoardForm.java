package com.studyIn.domain.board;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class BoardForm {

    @NotBlank @Size(max = 30)
    private String title;

    private String content;
}

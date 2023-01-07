package com.studyIn.domain.study.repository.query;

import lombok.Data;

@Data
public class StudyTagDto {

    private Long tagId;
    private String title;

    public StudyTagDto(Long tagId, String title) {
        this.tagId = tagId;
        this.title = title;
    }
}

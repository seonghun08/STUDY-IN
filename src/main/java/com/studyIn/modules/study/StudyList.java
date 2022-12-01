package com.studyIn.modules.study;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudyList {

    private List<Study> studyListManagerOf = new ArrayList<>();

    private List<Study> studyListMemberOf = new ArrayList<>();

    private Page<Study> studyPageManagerOf;

    private Page<Study> studyPageMemberOf;
}

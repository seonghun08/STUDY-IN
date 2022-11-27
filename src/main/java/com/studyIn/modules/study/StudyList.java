package com.studyIn.modules.study;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudyList {

    private List<Study> studyListManagerOf = new ArrayList<>();

    private List<Study> studyListMemberOf = new ArrayList<>();
}

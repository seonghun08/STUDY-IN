package com.studyIn.domain.study.controller;

import com.studyIn.domain.study.dto.form.TitleForm;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class TitleFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return TitleForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TitleForm titleForm = (TitleForm) target;

        if (studyRepository.existsByTitle(titleForm.getTitle())) {
            errors.rejectValue("title", "invalid.title", "이미 존재하는 스터디 이름입니다.");
        }
    }
}
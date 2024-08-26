package com.studyIn.domain.study.controller.validator;

import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;

        if (studyRepository.existsByTitle(studyForm.getTitle())) {
            errors.rejectValue("title", "invalid.title", "이미 존재하는 스터디 이름입니다.");
        }

        if (studyRepository.existsByPath(studyForm.getPath())) {
            errors.rejectValue("path", "invalid.path", "이미 존재하는 스터디 경로입니다.");
        }
    }
}

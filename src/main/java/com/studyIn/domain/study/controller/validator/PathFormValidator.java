package com.studyIn.domain.study.controller.validator;

import com.studyIn.domain.study.dto.form.PathForm;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PathFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return PathForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PathForm pathForm = (PathForm) target;

        if (studyRepository.existsByPath(pathForm.getPath())) {
            errors.rejectValue("path", "wrong.value", "이미 존재하는 스터디 경로입니다.");
        }
    }
}

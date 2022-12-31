package com.studyIn.domain.account.controller.validator;

import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;
    private final AuthenticationRepository authenticationRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;

        if (accountRepository.existsByUsername(signUpForm.getUsername())) {
            errors.rejectValue("username", "invalid.username", "이미 사용중인 아이디입니다.");
        }

        if (authenticationRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", "이미 사용중인 이메일입니다.");
        }
    }
}

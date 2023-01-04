package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.dto.form.UsernameForm;
import com.studyIn.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UsernameFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UsernameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UsernameForm usernameForm = (UsernameForm) target;

        if (isEqualsUsername(usernameForm)) {
            errors.rejectValue("username", "invalid.username", "현재 본인이 사용하고 있는 아이디입니다.");
        } else if (accountRepository.existsByUsername(usernameForm.getUsername())) {
            errors.rejectValue("username", "invalid.username", "이미 존재하는 아이디입니다.");
        }
    }

    private boolean isEqualsUsername(UsernameForm usernameForm) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return username.equals(usernameForm.getUsername());
    }
}

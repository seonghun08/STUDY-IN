package com.studyIn.domain.account.controller.validator;

import com.studyIn.domain.account.dto.form.EmailForm;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailFormValidator implements Validator {

    private final AuthenticationRepository authenticationRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return EmailForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EmailForm emailForm = (EmailForm) target;
        Optional<Authentication> authentication = authenticationRepository.findByEmail(emailForm.getEmail());

        if (authentication.isEmpty()) {
            errors.rejectValue("email", "invalid.email", "유효하지 않는 이메일입니다.");
        } else if (!authentication.get().canSendConfirmEmail()) {
            errors.rejectValue("email", "invalid.email", "인증 보내기는 1~2분 마다 사용할 수 있습니다.");
        }
    }
}

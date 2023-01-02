package com.studyIn.domain.account.controller.validator;

import com.studyIn.domain.account.dto.form.ResetPasswordForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ResetPasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ResetPasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ResetPasswordForm resetPasswordForm = (ResetPasswordForm) target;

        if (!isEqualPassword(resetPasswordForm)) {
            errors.rejectValue("password", "invalid.password", "입력한 패스워드가 서로 일치하지 않습니다.");
        }
    }

    /**
     * 새 패스워드와 확인 패스워드가 서로 일치하는가
     */
    private boolean isEqualPassword(ResetPasswordForm resetPasswordForm) {
        return resetPasswordForm.getPassword().equals(resetPasswordForm.getConfirmPassword());
    }
}

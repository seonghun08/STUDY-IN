package com.studyIn.modules.account.validator;

import com.studyIn.modules.account.form.ChangePasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangePasswordForm changePasswordForm = (ChangePasswordForm) target;

        if (!changePasswordForm.isEqualsPassword()) {
            errors.rejectValue("password", "invalid.password", "입력한 패스워드가 서로 일치하지 않습니다.");
        }
    }
}

package com.studyIn.modules.account.validator;


import com.studyIn.modules.account.form.PasswordForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PasswordFormValidator implements Validator {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;

        if (!isEqualsCurrentPassword(passwordForm)) {
            errors.rejectValue("currentPassword", "invalid.currentPassword", "현재 패스워드와 일치하지 않습니다.");
        }

        if (!isEqualsNewPasswordAndConfirmPassword(passwordForm)) {
            errors.rejectValue("newPassword", "invalid.newPassword", "입력한 새 패스워드가 서로 일치하지 않습니다.");
        }
    }

    private boolean isEqualsNewPasswordAndConfirmPassword(PasswordForm passwordForm) {
        return passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword());
    }

    private boolean isEqualsCurrentPassword(PasswordForm passwordForm) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String password = ((UserDetails) principal).getPassword();

        return passwordEncoder.matches(passwordForm.getCurrentPassword(), password);
    }
}

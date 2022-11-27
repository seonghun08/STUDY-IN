package com.studyIn.modules.account.validator;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.AccountRepository;
import com.studyIn.modules.account.form.EmailForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class EmailFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return EmailForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EmailForm emailForm = (EmailForm) target;
        Account account = accountRepository.findByEmail(emailForm.getEmail());

        if (account == null) {
            errors.rejectValue("email", "invalid.email", "유효하지 않는 이메일입니다.");
        } else if (!account.canSendConfirmEmail()) {
            errors.rejectValue("email", "invalid.email", "인증 보내기는 10분마다 사용할 수 있습니다.");
        }
    }
}

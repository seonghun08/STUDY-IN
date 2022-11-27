package com.studyIn.modules.account.form;

import com.studyIn.modules.account.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ChangePasswordForm {

    private String username;

    @NotBlank @Size(min = 8, max = 50)
    private String password;

    @NotBlank @Size(min = 8, max = 50)
    private String confirmPassword;

    public boolean isEqualsPassword() {
        return this.password.equals(this.confirmPassword);
    }

    public ChangePasswordForm(Account account) {
        this.username = account.getUsername();
    }
}

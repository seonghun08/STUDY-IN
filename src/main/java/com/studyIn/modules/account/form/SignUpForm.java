package com.studyIn.modules.account.form;

import com.studyIn.modules.account.GenderType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SignUpForm {

    @NotBlank @Size(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String nickname;

    @NotBlank @Size(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String username;

    @NotBlank @Email
    private String email;

    private GenderType genderType;

    private String birthday;

    @NotBlank @Size(min = 8, max = 50)
    private String password;

    private int age;

    public void putInIntegerAge() {
        LocalDate parsedBirthDate = LocalDate.of(
                Integer.parseInt(this.birthday.substring(0, 4)),
                Integer.parseInt(this.birthday.substring(5, 7)),
                Integer.parseInt(this.birthday.substring(8)));
        LocalDate now = LocalDate.now();
        int age = now.minusYears(parsedBirthDate.getYear()).getYear();

        if (parsedBirthDate.plusYears(age).isAfter(now)) {
            age -= 1;
        }

        this.age = age;
    }
}

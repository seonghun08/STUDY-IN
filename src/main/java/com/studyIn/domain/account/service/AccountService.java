package com.studyIn.domain.account.service;

import com.studyIn.domain.account.Address;
import com.studyIn.domain.account.NotificationSettings;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.User;
import com.studyIn.domain.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;

    public Long signUp(SignUpForm signUpForm) {

        Authentication authentication = new Authentication(
                signUpForm.getCellPhone(),
                signUpForm.getBirthday(),
                signUpForm.getGender(),
                new Address(signUpForm.getCity(), signUpForm.getCity(), signUpForm.getZipcode()),
                new NotificationSettings()
        );
        Profile profile = new Profile(signUpForm.getNickname());
        User user = User.createUser(signUpForm, profile, authentication);
        User saveUser = userRepository.save(user);
        return saveUser.getId();
    }
}

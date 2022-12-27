package com.studyIn.domain.account.entity;

import com.studyIn.domain.account.service.SignUpForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_user")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "authentication_id")
    private Authentication authentication;

    public static User createUser(SignUpForm signUpForm, Profile profile, Authentication authentication) {
        User user = new User();
        user.username = signUpForm.getUsername();
        user.email = signUpForm.getEmail();
        // passwordEncoder 처리 해야함
        user.password = signUpForm.getPassword();
        user.profile = profile;
        user.authentication = authentication;
        return user;
    }
}

package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.dto.form.SignUpForm;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
public class Account extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "authentication_id")
    private Authentication authentication;

    //== 연관관계 메서드 ==//
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        authentication.setAccount(this);
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    //== 생성 메서드 ==//
    public static Account createUser(SignUpForm signUpForm, Profile profile, Authentication authentication) {
        Account account = new Account();
        account.username = signUpForm.getUsername();
        account.password = signUpForm.getPassword();
        account.profile = profile;
        account.setAuthentication(authentication);
        return account;
    }

    /**
     * "PasswordEncoder" 패스워드 암호화
     */
    public void encodePassword(PasswordEncoder passwordEncoder){
        this.password = passwordEncoder.encode(password);
    }
}

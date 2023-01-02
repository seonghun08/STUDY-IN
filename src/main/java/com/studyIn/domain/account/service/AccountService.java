package com.studyIn.domain.account.service;

import com.studyIn.domain.account.UserAccount;
import com.studyIn.domain.account.dto.form.EmailForm;
import com.studyIn.domain.account.dto.form.ResetPasswordForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    /**
     * 회원가입
     */
    public Account signUp(SignUpForm form) {
        Account account = createAccount(form);
        sendConfirmEmail(account, "verify");
        return account;
    }

    /**
     * 계정 생성
     */
    private Account createAccount(SignUpForm form) {
        NotificationsSetting notificationsSetting = new NotificationsSetting();
        Authentication authentication = Authentication.createAuthentication(form, notificationsSetting);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);
        Account saveAccount = accountRepository.save(account);
        return saveAccount;
    }

    /**
     * 인증 메일 보내기
     */
    public void sendSignUpConfirmEmail(Long accountId) {
        Account account = accountRepository.findWithAuthenticationById(accountId)
                .orElseThrow(IllegalArgumentException::new);
        sendConfirmEmail(account, "verify");
    }

    public void sendLoginLinkConfirmEmail(EmailForm emailForm) {
        Account account = accountRepository.findWithAuthenticationByEmail(emailForm.getEmail())
                .orElseThrow(IllegalArgumentException::new);
        sendConfirmEmail(account, "loginLink");
    }

    private void sendConfirmEmail(Account account, String subject) {
        Authentication authentication = account.getAuthentication();
        authentication.generateEmailToken();

        if (subject.equals("verify")) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(account.getAuthentication().getEmail());
            mailMessage.setSubject("회원가입 인증");
            mailMessage.setText("/check-email-token?"
                    + "email=" + authentication.getEmail()
                    + "&token=" + authentication.getEmailCheckToken());
            javaMailSender.send(mailMessage);
        }

        if (subject.equals("loginLink")) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(account.getAuthentication().getEmail());
            mailMessage.setSubject("스터디인 로그인 링크");
            mailMessage.setText("/find-by-email?"
                    + "email=" + authentication.getEmail()
                    + "&token=" + authentication.getEmailCheckToken());
            javaMailSender.send(mailMessage);
        }

    }

    /**
     * email 존재 여부 확인 후, 토근이 일치하면 인증 정보 업데이트
     */
    public boolean completeSignUp(String email, String token) {
        Optional<Account> account = accountRepository.findByEmail(email);
        AtomicBoolean check = new AtomicBoolean(false);
        account.ifPresent(a -> {
            if (a.getAuthentication().isValidToken(token)) {
                a.getAuthentication().confirmEmailAuthentication();
                check.set(true);
            }
        });
        return check.get();
    }

    /**
     * 새로운 패스워드로 변경 후 로그인, 재사용 방지 인증 토큰 값 초기화
     */
    public void resetPassword(ResetPasswordForm resetPasswordForm) {
        Account account = accountRepository.findByEmail(resetPasswordForm.getEmail())
                .orElseThrow(IllegalArgumentException::new);

        account.updatePassword(passwordEncoder, resetPasswordForm.getPassword());
        account.getAuthentication().generateEmailToken();
        setAuthSession(account, resetPasswordForm.getPassword());
    }

    /**
     * "UserAccount" 세션 등록, 로그인
     */
    public void setAuthSession(Account account, String password) {
        var authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        new UserAccount(account),
                        password,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                ));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
    }
}

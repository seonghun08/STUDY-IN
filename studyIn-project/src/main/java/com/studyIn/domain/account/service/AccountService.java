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
import com.studyIn.infra.config.AppProperties;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final EmailService emailService;
    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

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
        accountRepository.findWithAuthenticationById(accountId)
                .ifPresent(account -> sendConfirmEmail(account, "verify"));
    }

    public void sendLoginLinkConfirmEmail(EmailForm emailForm) {
        accountRepository.findWithAuthenticationByEmail(emailForm.getEmail())
                .ifPresent(account -> sendConfirmEmail(account, "loginLink"));
    }

    private void sendConfirmEmail(Account account, String type) {
        Authentication authentication = account.getAuthentication();
        authentication.generateEmailToken();

        String subject = "";
        Context context = new Context();
        context.setVariable("host", appProperties.getHost());
        context.setVariable("username", account.getUsername());

        if (type.equals("verify")) {
            subject = "STUDY IN 회원가입 인증";
            context.setVariable("link", "/check-email-token?"
                    + "email=" + authentication.getEmail()
                    + "&token=" + authentication.getEmailCheckToken());
            context.setVariable("linkName", "이메일 인증하기");
            context.setVariable("message", "스터디인 서비스를 이용하시려면 링크를 클릭하세요.");
        }
        if (type.equals("loginLink")) {
            subject = "STUDY IN 로그인 링크";
            context.setVariable("link", "/find-by-email?"
                    + "email=" + authentication.getEmail()
                    + "&token=" + authentication.getEmailCheckToken());
            context.setVariable("linkName", "패스워드 재설정");
            context.setVariable("message", "잃어버린 패스워드를 재설정 하시려면 링크를 클릭하세요.");
        }

        if (!subject.isBlank()) {
            String message = templateEngine.process("mail/mail-link", context);
            EmailMessage emailMessage = EmailMessage.builder()
                    .to(authentication.getEmail())
                    .subject(subject)
                    .message(message)
                    .build();
            emailService.send(emailMessage);
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

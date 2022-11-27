package com.studyIn.modules.account;

import com.studyIn.infra.config.AppProperties;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import com.studyIn.modules.account.form.*;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.tag.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public Long count() {
        return accountRepository.count();
    }

    public Account findByEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new IllegalArgumentException(email + "에 해당하는 이용자가 없습니다.");
        }
        return account;
    }

    public Account findByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new IllegalArgumentException(username + "에 해당하는 이용자가 없습니다.");
        }
        return account;
    }

    public Account getTagsAndLocationsOnAccount(Account account) {
        return accountRepository.findAccountWithTagsAndLocationsById(account.getId());
    }

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    public void completeSignUp(Account account) {
        account.checkedEmail();
        login(account);
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        signUpForm.putInIntegerAge();
        Account account = modelMapper.map(signUpForm, Account.class);
        return accountRepository.save(account);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public void sendSignUpConfirmEmail(Account account) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        findAccount.ifPresent(a -> a.generateEmailCheckToken());

        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" +
                            findAccount.get().getEmailCheckToken() + "&email=" + findAccount.get().getEmail());
        context.setVariable("username", findAccount.get().getUsername());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디인 서비스를 이용하시려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/mail-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("STUDY IN, 회원가입 인증확인")
                .message(message)
                .build();

        emailService.send(emailMessage);
    }

    public void sendLoginLink(EmailForm emailForm) {
        Account account = accountRepository.findByEmail(emailForm.getEmail());
        account.generateEmailCheckToken();

        Context context = new Context();
        context.setVariable("link", "/find-by-email?token=" +
                            account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("username", account.getUsername());
        context.setVariable("linkName", "패스워드 재설정");
        context.setVariable("message", "잃어버린 패스워드를 재설정 하시려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/mail-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("STUDY IN, 패스워드 재설정 인증확인")
                .message(message)
                .build();

        emailService.send(emailMessage);
    }

    public void resetPassword(ChangePasswordForm changePasswordForm) {
        Account account = accountRepository.findByUsername(changePasswordForm.getUsername());
        account.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
        account.generateEmailCheckToken();
        accountRepository.save(account);
        login(account);
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(emailOrUsername);

        if (account == null) {
            account = accountRepository.findByEmail(emailOrUsername);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrUsername);
        }

        return new UserAccount(account);
    }


    /**
     * settingsController Service method
     */
    public void updateProfile(ProfileForm profileForm, Account account) {
        modelMapper.map(profileForm, account);
        accountRepository.save(account);
    }

    public void updatePassword(PasswordForm passwordForm, Account account) {
        account.setPassword(passwordEncoder.encode(passwordForm.getNewPassword()));
        accountRepository.save(account);
    }

    public void updateNotifications(NotificationsForm notificationsForm, Account account) {
        modelMapper.map(notificationsForm, account);
        accountRepository.save(account);
    }

    public void updateUsername(UsernameForm usernameForm, Account account) {
        modelMapper.map(usernameForm, account);
        accountRepository.save(account);
        login(account);
    }

    public Set<Tag> findTags(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void addTag(Tag tag, Account account) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().add(tag));
    }

    public void removeTag(Tag tag, Account account) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Location> findLocations(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getLocations();
    }

    public void addLocation(Location location, Account account) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getLocations().add(location));
    }

    public void removeLocation(Location location, Account account) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getLocations().remove(location));
    }
}

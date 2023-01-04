package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.controller.validator.EmailFormValidator;
import com.studyIn.domain.account.controller.validator.ResetPasswordFormValidator;
import com.studyIn.domain.account.controller.validator.SignUpFormValidator;
import com.studyIn.domain.account.dto.TotalCountAndNicknameDto;
import com.studyIn.domain.account.dto.ProfileDto;
import com.studyIn.domain.account.dto.form.EmailForm;
import com.studyIn.domain.account.dto.form.ResetPasswordForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.query.AccountQueryRepository;
import com.studyIn.domain.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final AuthenticationRepository authenticationRepository;
    private final SignUpFormValidator signUpFormValidator;
    private final EmailFormValidator emailFormValidator;
    private final ResetPasswordFormValidator resetPasswordFormValidator;

    @InitBinder("signUpForm")
    public void signUpFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @InitBinder("emailForm")
    public void emailFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(emailFormValidator);
    }

    @InitBinder("resetPasswordForm")
    public void changePasswordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(resetPasswordFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("sign-up")
    public String signUp(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.signUp(signUpForm);
        accountService.setAuthSession(account, signUpForm.getPassword());
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(@CurrentAccount AccountInfo accountInfo, String email, String token, Model model) {
        if (!accountService.completeSignUp(email, token)) {
            model.addAttribute("error", "잘못된 접근입니다.");
            return "account/checked-email";
        }

        if (accountInfo != null) {
            model.addAttribute(accountInfo);
            accountInfo.setEmailVerified(true);
        }

        TotalCountAndNicknameDto dto = accountQueryRepository.findCountAndNicknameByEmail(email);
        model.addAttribute("numberOfUser", dto.getTotalCount());
        model.addAttribute("nickname", dto.getNickname());
        return "account/checked-email";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount AccountInfo accountInfo, Model model) {
        model.addAttribute("email", accountInfo.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount AccountInfo accountInfo, Model model) {
        Authentication authentication = authenticationRepository.findById(accountInfo.getAuthenticationId())
                .orElseThrow(IllegalArgumentException::new);

        if (!authentication.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1~2분 마다 한번 씩 보낼 수 있습니다.");
            model.addAttribute("email", accountInfo.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(accountInfo.getAccountId());
        return "redirect:/";
    }

    @GetMapping("/profile/{username}")
    public String viewProfile(@CurrentAccount AccountInfo accountInfo, @PathVariable String username, Model model) {
        ProfileDto profileDTO = accountRepository.findProfileDtoByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(username + "에 해당하는 사용자가 없습니다."));
        model.addAttribute(accountInfo);
        model.addAttribute("profile", profileDTO);
        model.addAttribute("isOwner", profileDTO.isOwner(accountInfo.getUsername()));
        return "account/profile";
    }

    @GetMapping("find-password")
    public String findPasswordForm(Model model) {
        model.addAttribute(new EmailForm());
        return "account/find-password";
    }

    @PostMapping("find-password")
    public String findPassword(@Valid EmailForm emailForm, Errors errors, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            return "account/find-password";
        }

        accountService.sendLoginLinkConfirmEmail(emailForm);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/find-password";
    }

    @GetMapping("/find-by-email")
    public String resetPasswordForm(String email, String token, Model model) {
        Optional<Authentication> authentication = authenticationRepository.findByEmail(email);

        if (authentication.isEmpty()) {
            model.addAttribute("error", "잘못된 접근입니다.");
            return "account/find-by-email";
        }

        if (!authentication.orElseThrow().isValidToken(token)) {
            model.addAttribute("error", "인증 토큰이 일치하지 않습니다");
            return "account/find-by-email";
        }

        model.addAttribute(new ResetPasswordForm(email));
        return "account/find-by-email";
    }

    @PostMapping("/find-by-email")
    public String loginByChangePassword(@Valid ResetPasswordForm resetPasswordForm, Errors errors, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            return "account/find-by-email";
        }

        accountService.resetPassword(resetPasswordForm);
        attributes.addFlashAttribute("message", "비밀번호 변경이 완료되었습니다.");
        return "redirect:/";
    }
}

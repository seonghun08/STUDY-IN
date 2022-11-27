package com.studyIn.modules.account;

import com.studyIn.modules.account.form.ChangePasswordForm;
import com.studyIn.modules.account.form.EmailForm;
import com.studyIn.modules.account.form.SignUpForm;
import com.studyIn.modules.account.validator.ChangePasswordFormValidator;
import com.studyIn.modules.account.validator.EmailFormValidator;
import com.studyIn.modules.account.validator.SignUpFormValidator;
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

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;
    private final EmailFormValidator emailFormValidator;
    private final ChangePasswordFormValidator changePasswordFormValidator;


    @InitBinder("signUpForm")
    public void signUpFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @InitBinder("emailForm")
    public void emailFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(emailFormValidator);
    }

    @InitBinder("changePasswordForm")
    public void changePasswordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(changePasswordFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountService.findByEmail(email);
        String view = "account/checked-email";

        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountService.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping("check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 10분에 한번만 전송할 수 있습니다.");
            model.addAttribute(account);
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{username}")
    public String viewProfile(@CurrentAccount Account account, @PathVariable String username, Model model) {
        Account findAccount = accountService.findByUsername(username);
        model.addAttribute(account);
        model.addAttribute("isOwner", findAccount.equals(account));
        model.addAttribute("profile", findAccount);
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

        accountService.sendLoginLink(emailForm);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/find-password";
    }

    @GetMapping("/find-by-email")
    public String changePasswordForm(String token, String email, Model model) {
        Account account = accountService.findByEmail(email);
        String view = "account/find-by-email";

        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error", "잘못된 접근입니다.");
            return view;
        }

        model.addAttribute(new ChangePasswordForm(account));
        return view;
    }

    @PostMapping("/find-by-email")
    public String loginByChangePassword(@Valid ChangePasswordForm changePasswordForm, Errors errors, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            return "account/find-by-email";
        }

        accountService.resetPassword(changePasswordForm);
        attributes.addFlashAttribute("message", "비밀번호 변경이 완료되었습니다.");
        return "redirect:/";
    }

}

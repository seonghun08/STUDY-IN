package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.controller.validator.SignUpFormValidator;
import com.studyIn.domain.account.dto.AccountDTO;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.form.SignUpForm;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.query.AccountQueryRepository;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.account.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final LoginService loginService;

    @InitBinder("signUpForm")
    public void signUpFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
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
        loginService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String email, String token, Model model) {
        String view = "account/checked-email";

        /**
         * email 인증 토근 확인 후 인증 정보 업데이트
         */
        if (!accountService.completeSignUp(email, token)) {
            model.addAttribute("error", "wrong.access");
            return view;
        }

        AccountDTO accountDTO = accountQueryRepository.findCountAndNicknameByEmail(email);
        model.addAttribute("numberOfUser", accountDTO.getTotalCount());
        model.addAttribute("nickname", accountDTO.getNickname());
        return view;
    }


}

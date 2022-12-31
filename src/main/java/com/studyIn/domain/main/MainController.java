package com.studyIn.domain.main;

import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.AccountInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentAccount AccountInfo accountInfo, Model model) {
        if (accountInfo != null) {
            model.addAttribute(accountInfo);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

package com.studyIn.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String username) {
        Account account = new Account();
        account.setUsername(username);
        account.setNickname(username);
        account.setEmail(username + "@email.com");
        account.setPassword("1234567890");
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }
}

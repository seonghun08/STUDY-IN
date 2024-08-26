package com.studyIn.domain.account;

import com.studyIn.domain.account.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAccount extends User {

    private AccountInfo accountInfo;

    public UserAccount(Account account) {
        super(account.getUsername(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.accountInfo = new AccountInfo(account);
    }
}

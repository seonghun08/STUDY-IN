package com.studyIn.domain.account.service;

import com.studyIn.domain.account.UserAccount;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<Account> account;

        if (accountRepository.existsByUsername(usernameOrEmail)) {
            account = accountRepository.findByUsername(usernameOrEmail);
        } else {
            account = accountRepository.findByEmail(usernameOrEmail);
        }

        if (account.isEmpty()) {
            throw new UsernameNotFoundException(usernameOrEmail);
        }

        return new UserAccount(account.get());
    }
}

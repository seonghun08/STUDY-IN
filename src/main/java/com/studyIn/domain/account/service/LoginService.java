package com.studyIn.domain.account.service;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AccountRepository accountRepository;

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        account.getUsername(),
                        account.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}

package com.studyIn.infra.config;

import com.studyIn.domain.account.UserAccount;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoginUserAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (null == authentication || !authentication.isAuthenticated()) {
            return null;
        }

        UserAccount userAccount = (UserAccount) authentication.getPrincipal();
        return Optional.of(userAccount.getAccountInfo().getAccountId());
    }

}

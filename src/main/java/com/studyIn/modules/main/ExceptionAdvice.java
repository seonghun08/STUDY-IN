package com.studyIn.modules.main;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.CurrentAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentAccount Account account, HttpServletRequest request, RuntimeException e) {
        if (account != null) {
            log.info("account: {}, requested: {}", account.getUsername(), request.getRequestURI());
        } else {
            log.info("account: null, requested: {}", request.getRequestURI());
        }

        log.error("bad request", e);
        return "error";
    }
}

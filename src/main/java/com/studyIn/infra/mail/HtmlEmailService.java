package com.studyIn.infra.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class HtmlEmailService implements EmailService {

    @Override
    public void send(EmailMessage emailMessage) {

    }
}

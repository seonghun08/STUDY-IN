package com.studyIn;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class test {

    @Test
    public void date() throws Exception {
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(10);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(35);

        long totalTime = ChronoUnit.DAYS.between(startDateTime, endDateTime);
        long pastTime = ChronoUnit.DAYS.between(startDateTime, LocalDateTime.now());

        long result = (long) (((double) pastTime / totalTime) * 100);
        long between = ChronoUnit.MINUTES.between(LocalDateTime.now(), startDateTime);

        log.info("th:style=\"{}%\"", result);

        if (between < 0) {
            log.info("{}", true);
        } else {
            log.info("{}", false);
        }
    }

    @Test
    public void birthday() throws Exception {
        String birthday = "1997-08-30";
        LocalDate now = LocalDate.now();

        LocalDate parsedBirthDate = LocalDate.of(
                Integer.parseInt(birthday.substring(0, 4)),
                Integer.parseInt(birthday.substring(5, 7)),
                Integer.parseInt(birthday.substring(8)));
        int age = now.minusYears(parsedBirthDate.getYear()).getYear();

        if (parsedBirthDate.plusYears(age).isAfter(now)) {
            age -= 1;
        }

    }
}

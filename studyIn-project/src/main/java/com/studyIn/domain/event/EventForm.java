package com.studyIn.domain.event;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class EventForm {

    @NotBlank(message = "필수 정보입니다.")
    @Size(max = 30, message = "30자 이내로 입력하세요.")
    private String title;

    private String description;

    @Min(2)
    private int limitOfEnrollments = 2;

    private EventType eventType = EventType.FIRST_COME_FIRST_SERVED;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endEnrollmentDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
}

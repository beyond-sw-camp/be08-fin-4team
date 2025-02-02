package com.beyond.easycheck.events.ui.requestbody;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {

    @NotNull
    private Long accommodationId;

    @NotBlank
    private String eventName;

    @NotBlank
    private String detail;

    @NotNull(message = "이벤트 시작 날짜를 지정해야 합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "이벤트 종료 날짜를 지정해야 합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

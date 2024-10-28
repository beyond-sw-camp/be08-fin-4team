package com.beyond.easycheck.payments.ui.requestbody;

import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
public class PaymentCreateRequest {

    @NotBlank
    private String impUid;

    @NotNull
    private Long reservationId;

    @NotBlank
    private String method;

    @NotNull
    @Min(value = 0, message = "price must be greater than or equal to 0")
    private Integer amount;

    private String bank;

    private String accountHolder;

    private LocalDateTime depositDeadline;

    @NotNull
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private CompletionStatus completionStatus;
}

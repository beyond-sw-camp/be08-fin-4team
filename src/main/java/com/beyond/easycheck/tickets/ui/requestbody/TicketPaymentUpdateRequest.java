package com.beyond.easycheck.tickets.ui.requestbody;

import com.beyond.easycheck.tickets.infrastructure.entity.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class TicketPaymentUpdateRequest {

    @NotBlank
    private String impUid;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}

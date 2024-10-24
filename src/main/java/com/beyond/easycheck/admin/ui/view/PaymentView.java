package com.beyond.easycheck.admin.ui.view;

import com.beyond.easycheck.admin.application.service.AdminReadUseCase;
import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

import static com.beyond.easycheck.admin.application.service.AdminReadUseCase.*;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentView {
    private final Long id;
    private final String impUid;
    private final String username;
    private final String userRole;
    private final Long reservationRoomId;
    private final LocalDateTime checkinDate;
    private final LocalDateTime checkoutDate;
    private final String method;
    private final Integer amount;
    private final CompletionStatus completionStatus;

    public PaymentView(FindPaymentResult result) {
        this.id = result.id();
        this.impUid = result.impUid();
        this.username = result.username();
        this.userRole = result.userRole();
        this.reservationRoomId = result.reservationRoomId();
        this.checkinDate = result.checkinDate();
        this.checkoutDate = result.checkoutDate();
        this.method = result.method();
        this.amount = result.amount();
        this.completionStatus = result.completionStatus();
    }
}
package com.beyond.easycheck.tickets.ui.view;

import com.beyond.easycheck.tickets.infrastructure.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketPaymentView {

    private String impUid;

    private Long orderId;

    private String accommodationName;

    private String ticketName;

    private LocalDateTime validFromDate;

    private LocalDateTime validToDate;

    private int quantity;

    private PaymentStatus paymentStatus;

    private LocalDateTime cancelDate;

    private String paymentMethod;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentDate;
}

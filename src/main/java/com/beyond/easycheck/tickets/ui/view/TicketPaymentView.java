package com.beyond.easycheck.tickets.ui.view;

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

    private String paymentMethod;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentDate;
}

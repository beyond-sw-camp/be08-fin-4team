package com.beyond.easycheck.tickets.ui.view;

import com.beyond.easycheck.tickets.infrastructure.entity.PaymentStatus;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketPaymentEntity;
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

    private Long id;

    private String impUid;

    private Long orderId;

    private PaymentStatus paymentStatus;

    private String paymentMethod;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentDate;

    public static TicketPaymentView of(TicketPaymentEntity ticketPaymentEntity) {

        return new TicketPaymentView(

                ticketPaymentEntity.getId(),
                ticketPaymentEntity.getImpUid(),
                ticketPaymentEntity.getTicketOrder().getId(),
                ticketPaymentEntity.getPaymentStatus(),
                ticketPaymentEntity.getPaymentMethod(),
                ticketPaymentEntity.getPaymentAmount(),
                ticketPaymentEntity.getPaymentDate()
        );
    }
}

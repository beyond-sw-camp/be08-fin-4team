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

    private Long userId;

    private String userName;

    private int point;

    private String accommodationName;

    private String ticketName;

    private String themeParkName;

    private LocalDateTime validFromDate;

    private LocalDateTime validToDate;

    private int quantity;

    private PaymentStatus paymentStatus;

    private LocalDateTime cancelDate;

    private String paymentMethod;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentDate;

    public static TicketPaymentView of(TicketPaymentEntity ticketPaymentEntity) {

        return new TicketPaymentView(

                ticketPaymentEntity.getId(),
                ticketPaymentEntity.getImpUid(),
                ticketPaymentEntity.getTicketOrder().getId(),
                ticketPaymentEntity.getTicketOrder().getUserEntity().getId(),
                ticketPaymentEntity.getTicketOrder().getUserEntity().getName(),
                ticketPaymentEntity.getTicketOrder().getUserEntity().getPoint(),
                ticketPaymentEntity.getTicketOrder().getTicket().getThemePark().getAccommodation().getName(),
                ticketPaymentEntity.getTicketOrder().getTicket().getTicketName(),
                ticketPaymentEntity.getTicketOrder().getTicket().getThemePark().getName(),
                ticketPaymentEntity.getTicketOrder().getTicket().getValidFromDate(),
                ticketPaymentEntity.getTicketOrder().getTicket().getValidToDate(),
                ticketPaymentEntity.getTicketOrder().getQuantity(),
                ticketPaymentEntity.getPaymentStatus(),
                ticketPaymentEntity.getCancelDate(),
                ticketPaymentEntity.getPaymentMethod(),
                ticketPaymentEntity.getPaymentAmount(),
                ticketPaymentEntity.getPaymentDate()
        );
    }
}

package com.beyond.easycheck.tickets.ui.view;

import com.beyond.easycheck.tickets.infrastructure.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TicketOrderDTO {
    private final Long orderId;
    private final String ticketName;
    private final int quantity;
    private final BigDecimal totalPrice;
    private final Long userId;
    private final LocalDateTime purchaseTimestamp;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private OrderStatus orderStatus;

    public TicketOrderDTO(Long orderId, String ticketName, int quantity, BigDecimal totalPrice, Long userId, LocalDateTime purchaseTimestamp, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.ticketName = ticketName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.userId = userId;
        this.purchaseTimestamp = purchaseTimestamp;
        this.orderStatus = orderStatus;
    }
}
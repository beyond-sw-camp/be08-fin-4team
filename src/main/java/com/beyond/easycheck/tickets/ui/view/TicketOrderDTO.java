package com.beyond.easycheck.tickets.ui.view;

import com.beyond.easycheck.tickets.infrastructure.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderDTO {

    private Long id;
    private Long userId;
    private String collectionAgreement;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private LocalDateTime purchaseTimestamp;
    private List<OrderDetailsDTO> orderDetails;
    private String paymentMethod;
    private BigDecimal paymentAmount;
}

package com.beyond.easycheck.tickets.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "order_details")
public class OrderDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private TicketOrderEntity ticketOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal price;

    public OrderDetailsEntity(TicketOrderEntity ticketOrder, TicketEntity ticket, int quantity) {
        this.ticketOrder = ticketOrder;
        this.ticket = ticket;
        this.quantity = quantity;
        this.price = ticket.getPrice();
        ticketOrder.getOrderDetails().add(this);
    }
}

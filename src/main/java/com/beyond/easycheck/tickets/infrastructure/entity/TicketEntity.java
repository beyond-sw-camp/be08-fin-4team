package com.beyond.easycheck.tickets.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.themeparks.infrastructure.entity.ThemeParkEntity;
import com.beyond.easycheck.tickets.application.service.TicketOperationUseCase.TicketCreateCommand;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ticket")
@ToString(of = {"id", "ticketName", "price"})
public class TicketEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_park_id", nullable = false)
    private ThemeParkEntity themePark;

    @Column(nullable = false)
    private String ticketName;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime saleStartDate;

    @Column(nullable = false)
    private LocalDateTime saleEndDate;

    @Column(nullable = false)
    private LocalDateTime validFromDate;

    @Column(nullable = false)
    private LocalDateTime validToDate;

    public static TicketEntity createTicket(TicketCreateCommand command, ThemeParkEntity themePark) {
        return new TicketEntity(
                null,
                themePark,
                command.getTicketName(),
                command.getPrice(),
                command.getSaleStartDate(),
                command.getSaleEndDate(),
                command.getValidFromDate(),
                command.getValidToDate()
        );
    }

    private TicketEntity(Long id, ThemeParkEntity themePark, String ticketName, BigDecimal price,
                         LocalDateTime saleStartDate, LocalDateTime saleEndDate,
                         LocalDateTime validFromDate, LocalDateTime validToDate) {
        this.id = id;
        this.themePark = themePark;
        this.ticketName = ticketName;
        this.price = price;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.validFromDate = validFromDate;
        this.validToDate = validToDate;
    }

    public void update(String ticketName, BigDecimal price, LocalDateTime saleStartDate,
                       LocalDateTime saleEndDate, LocalDateTime validFromDate, LocalDateTime validToDate) {
        this.ticketName = ticketName;
        this.price = price;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.validFromDate = validFromDate;
        this.validToDate = validToDate;
    }
}
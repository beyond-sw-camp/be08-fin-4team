package com.beyond.easycheck.tickets.application.service;

import com.beyond.easycheck.tickets.infrastructure.entity.TicketEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketReadUseCase {
    List<FindTicketResult> getTicketsByThemePark(Long themeParkId);

    List<FindTicketResult> getTicketsByThemeParkOnSale(Long themeParkId);

    FindTicketResult getTicketById(Long ticketId);

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    class FindTicketResult {
        private Long id;
        private Long themeParkId;
        private String ticketName;
        private BigDecimal price;
        private LocalDateTime saleStartDate;
        private LocalDateTime saleEndDate;
        private LocalDateTime validFromDate;
        private LocalDateTime validToDate;

        public static FindTicketResult fromEntity(TicketEntity ticket) {
            return FindTicketResult.builder()
                    .id(ticket.getId())
                    .themeParkId(ticket.getThemePark().getId())
                    .ticketName(ticket.getTicketName())
                    .price(ticket.getPrice())
                    .saleStartDate(ticket.getSaleStartDate())
                    .saleEndDate(ticket.getSaleEndDate())
                    .validFromDate(ticket.getValidFromDate())
                    .validToDate(ticket.getValidToDate())
                    .build();
        }
    }
}

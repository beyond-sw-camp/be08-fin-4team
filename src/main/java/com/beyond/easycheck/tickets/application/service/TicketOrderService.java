package com.beyond.easycheck.tickets.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketOrderEntity;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketOrderRepository;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketEntity;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketRepository;
import com.beyond.easycheck.themeparks.infrastructure.repository.ThemeParkRepository;
import com.beyond.easycheck.tickets.ui.requestbody.TicketOrderRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.beyond.easycheck.themeparks.exception.ThemeParkMessageType.THEME_PARK_NOT_FOUND;
import static com.beyond.easycheck.tickets.exception.TicketMessageType.*;

@Service
@RequiredArgsConstructor
public class TicketOrderService {

    private final TicketOrderRepository ticketOrderRepository;
    private final TicketRepository ticketRepository;
    private final ThemeParkRepository themeParkRepository;

    @Transactional
    public TicketOrderEntity createTicketOrder(Long themeParkId, TicketOrderRequest request) {

        if (!themeParkRepository.existsById(themeParkId)) {
            throw new EasyCheckException(THEME_PARK_NOT_FOUND);
        }

        TicketEntity ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new EasyCheckException(TICKET_NOT_FOUND));

        if (!ticket.getThemePark().getId().equals(themeParkId)) {
            throw new EasyCheckException(TICKET_NOT_BELONG_TO_THEME_PARK);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(ticket.getSaleStartDate()) || now.isAfter(ticket.getSaleEndDate())) {
            throw new EasyCheckException(TICKET_SALE_PERIOD_INVALID);
        }

        if (request.getQuantity() <= 0) {
            throw new EasyCheckException(INVALID_QUANTITY);
        }

        TicketOrderEntity ticketOrder = new TicketOrderEntity(
                ticket,
                request.getQuantity(),
                request.getUserId(),
                request.getGuestId(),
                request.getReceiptMethod(),
                request.getCollectionAgreement()
        );

        return ticketOrderRepository.save(ticketOrder);
    }
}
package com.beyond.easycheck.tickets.ui.view;

import com.beyond.easycheck.themeparks.infrastructure.entity.ThemeParkEntity;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketEntity;
import com.beyond.easycheck.tickets.application.service.TicketReadUseCase.FindTicketResult;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class TicketView {
    private final Long id;
    private final Long themeParkId;
    private final String ticketName;
    private final BigDecimal price;
    private final LocalDateTime saleStartDate;
    private final LocalDateTime saleEndDate;
    private final LocalDateTime validFromDate;
    private final LocalDateTime validToDate;

    public TicketView(FindTicketResult result) {
        this.id = result.getId();
        this.themeParkId = result.getThemeParkId();
        this.ticketName = result.getTicketName();
        this.price = result.getPrice();
        this.saleStartDate = result.getSaleStartDate();
        this.saleEndDate = result.getSaleEndDate();
        this.validFromDate = result.getValidFromDate();
        this.validToDate = result.getValidToDate();
    }
}

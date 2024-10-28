package com.beyond.easycheck.tickets.ui.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDTO {

    private Long ticketId;
    private String ticketName;
    private int quantity;
    private BigDecimal price;
}

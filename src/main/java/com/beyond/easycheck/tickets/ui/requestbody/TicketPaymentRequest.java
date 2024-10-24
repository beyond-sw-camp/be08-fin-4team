package com.beyond.easycheck.tickets.ui.requestbody;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketPaymentRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotBlank(message = "impUid는 필수입니다.")
    private String impUid;

    @NotBlank(message = "결제 방법은 필수입니다.")
    private String paymentMethod;

    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    private BigDecimal paymentAmount;

    @NotNull(message = "결제 날짜는 필수입니다.")
    private LocalDateTime paymentDate;
}
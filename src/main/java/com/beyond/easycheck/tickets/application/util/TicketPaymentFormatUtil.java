package com.beyond.easycheck.tickets.application.util;

import com.beyond.easycheck.tickets.infrastructure.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketPaymentFormatUtil {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(dateTimeFormatter);
    }

    public static String formatPaymentStatus(PaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case COMPLETED:
                return "결제 완료";
            case REFUNDED:
                return "환불 완료";
            case PENDING:
                return "결제 대기";
            default:
                return "알 수 없음";
        }
    }
}

package com.beyond.easycheck.tickets.exception;

import com.beyond.easycheck.common.exception.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TicketOrderMessageType implements MessageType {

    ORDER_NOT_FOUND("해당 주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_CANCELLED("이미 취소된 주문입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_COMPLETED("이미 완료된 결제입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED("결제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("해당 결제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS_FOR_PAYMENT("주문이 결제 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS_FOR_COMPLETION("주문이 완료 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS_FOR_CANCELLATION("주문이 취소 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    ORDER_ALREADY_COMPLETED("이미 사용된 주문입니다.", HttpStatus.BAD_REQUEST),


    INVALID_PAYMENT_STATUS_FOR_COMPLETION("결제 완료 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS_FOR_FAILURE("완료되거나 취소된 결제는 실패 처리할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS_FOR_CANCELLATION("대기 중이거나 완료된 결제만 취소할 수 있습니다.", HttpStatus.BAD_REQUEST),

    INVALID_STATUS_FOR_REFUND("환불 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS_FOR_RETRY("재결제 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    TICKET_ORDER_CANNOT_BE_NULL("티켓 주문은 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_LINKED("주문이 이미 결제와 연결되어 있습니다.", HttpStatus.BAD_REQUEST),

    PORTONE_VERIFICATION_ERROR("PortOne 결제 검증 오류", HttpStatus.INTERNAL_SERVER_ERROR),
    PORTONE_REFUND_FAILED("PortOne 환불 처리 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
   private final String message;
   private final HttpStatus status;
}

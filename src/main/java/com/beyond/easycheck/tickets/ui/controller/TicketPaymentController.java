package com.beyond.easycheck.tickets.ui.controller;

import com.beyond.easycheck.common.ui.view.ApiResponseView;
import com.beyond.easycheck.tickets.application.service.TicketPaymentService;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketPaymentEntity;
import com.beyond.easycheck.tickets.ui.requestbody.TicketPaymentRequest;
import com.beyond.easycheck.tickets.ui.requestbody.TicketPaymentUpdateRequest;
import com.beyond.easycheck.tickets.ui.view.TicketPaymentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TicketPayment", description = "입장권 결제 정보 관리 API")
@RestController
@RequestMapping("/api/v1/tickets/payment")
@RequiredArgsConstructor
public class TicketPaymentController {

    private final TicketPaymentService ticketPaymentService;

    @Operation(summary = "입장권 결제 추가하는 API")
    @PostMapping("/{orderId}")
    public ResponseEntity<TicketPaymentView> processPayment(
            @PathVariable Long orderId,
            @RequestBody TicketPaymentRequest request,
            @AuthenticationPrincipal Long userId) {

        TicketPaymentView result = ticketPaymentService.processPayment(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @Operation(summary = "입장권 결제 내역 조회 API")
    @GetMapping("")
    public ResponseEntity<List<TicketPaymentView>> getAllTicketPayments() {

        List<TicketPaymentView> ticketPaymentViews = ticketPaymentService.getAllTicketPayments();

        return ResponseEntity.ok(ticketPaymentViews);
    }

    @Operation(summary = "입장권 결제 취소 API")
    @PutMapping("/{id}")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable("id") Long id,
            @RequestBody @Valid TicketPaymentUpdateRequest ticketPaymentUpdateRequest) {

            ticketPaymentService.cancelPayment(id, ticketPaymentUpdateRequest);

            return ResponseEntity.noContent().build();
    }

    @Operation(summary = "입장권 결제 상태 조회 API")
    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponseView<TicketPaymentEntity>> getPaymentStatus(
            @PathVariable Long orderId) {

        TicketPaymentEntity paymentStatus = ticketPaymentService.getPaymentStatus(orderId);
        return ResponseEntity.ok(new ApiResponseView<>(paymentStatus));
    }

    @Operation(summary = "사용자의 결제 내역 조회 API")
    @GetMapping("/history")
    public ResponseEntity<ApiResponseView<List<TicketPaymentEntity>>> getPaymentHistory(
            @AuthenticationPrincipal Long userId) {

        List<TicketPaymentEntity> paymentHistory = ticketPaymentService.getPaymentHistory(userId);
        return ResponseEntity.ok(new ApiResponseView<>(paymentHistory));
    }
}

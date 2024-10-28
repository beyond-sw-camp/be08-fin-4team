package com.beyond.easycheck.payments.ui.controller;

import com.beyond.easycheck.payments.application.service.PaymentService;
import com.beyond.easycheck.payments.ui.requestbody.PaymentCreateRequest;
import com.beyond.easycheck.payments.ui.requestbody.PaymentUpdateRequest;
import com.beyond.easycheck.payments.ui.requestbody.WebhookRequest;
import com.beyond.easycheck.payments.ui.view.PaymentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payment", description = "결제 관리")
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 처리하는 API")
    @PostMapping("")
    public ResponseEntity<Void> processPayment(@RequestBody @Valid PaymentCreateRequest paymentCreateRequest) {

        paymentService.processReservationPayment(paymentCreateRequest.getReservationId(), paymentCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "가상계좌 입금 확인 API")
    @PostMapping("/portone")
    public ResponseEntity<String> handlePortOneWebhook(@RequestBody WebhookRequest webhookRequest) {

        try {
            paymentService.handleVirtualAccountDeposit(webhookRequest.getImpUid());
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
        }
    }

    @Operation(summary = "결제 내역 리스트를 조회하는 API")
    @GetMapping("")
    public ResponseEntity<List<PaymentView>> getAllPayments() {

        List<PaymentView> payments = paymentService.getAllPayments();

        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "특정 결제 내역을 조회하는 API")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentView> getPaymentById(@PathVariable("id") Long id) {

        PaymentView paymentView = paymentService.getPaymentById(id);

        return ResponseEntity.ok(paymentView);
    }

    @Operation(summary = "결제를 환불하는 API")
    @PutMapping("/{id}")
    public ResponseEntity<Void> cancelPayment(@PathVariable("id") Long id,
                                              @RequestBody @Valid PaymentUpdateRequest paymentUpdateRequest) {

        paymentService.cancelPayment(id, paymentUpdateRequest);

        return ResponseEntity.noContent().build();
    }
}


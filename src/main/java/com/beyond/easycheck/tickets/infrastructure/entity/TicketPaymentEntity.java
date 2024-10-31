package com.beyond.easycheck.tickets.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.beyond.easycheck.tickets.exception.TicketOrderMessageType.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_payment")
@ToString(of = {"id", "impUid", "paymentAmount", "paymentMethod", "bank", "accountHolder", "depositDeadline"})
public class TicketPaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_order_id", nullable = false)
    private TicketOrderEntity ticketOrder;

    @Column(nullable = false)
    private String impUid;

    @Column(name = "amount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = true)
    private String bank;

    @Column(nullable = true)
    private String accountHolder;

    @Column(nullable = true)
    private LocalDateTime depositDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    private LocalDateTime cancelDate;

    public TicketPaymentEntity(TicketOrderEntity order, String impUid, BigDecimal amount, String bank, String accountHolder, LocalDateTime depositDeadline, String method) {
        if (order == null) {
            throw new EasyCheckException(TICKET_ORDER_CANNOT_BE_NULL);
        }
        this.impUid = impUid;
        this.ticketOrder = order;
        this.paymentAmount = amount;
        this.bank = bank;
        this.accountHolder = accountHolder;
        this.depositDeadline = depositDeadline;
        this.paymentMethod = method;
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentDate = LocalDateTime.now();
    }

    public void completePayment() {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new EasyCheckException(INVALID_PAYMENT_STATUS_FOR_COMPLETION);
        }
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    public void updatePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void failPayment() {
        if (this.paymentStatus == PaymentStatus.COMPLETED || this.paymentStatus == PaymentStatus.CANCELLED) {
            throw new EasyCheckException(INVALID_PAYMENT_STATUS_FOR_FAILURE);
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }
}
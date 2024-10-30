package com.beyond.easycheck.tickets.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.beyond.easycheck.tickets.infrastructure.entity.OrderStatus.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ticket_order")
public class TicketOrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(nullable = false)
    @Min(1)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptMethodType receiptMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionAgreementType collectionAgreement;

    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime purchaseTimestamp;

    // New fields for buyer information
    @Column(nullable = false)
    @NotBlank(message = "Buyer name is required")
    private String buyerName;

    @Column(nullable = false)
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number format")
    private String buyerPhone;

    @Column(nullable = false)
    @Email(message = "Invalid email format")
    private String buyerEmail;

    public TicketOrderEntity(TicketEntity ticket, int quantity, UserEntity userEntity, ReceiptMethodType receiptMethod,
                             CollectionAgreementType collectionAgreement, String buyerName, String buyerPhone, String buyerEmail) {
        this.ticket = ticket;
        this.quantity = quantity;
        this.userEntity = userEntity;
        this.receiptMethod = receiptMethod;
        this.collectionAgreement = collectionAgreement;
        this.totalPrice = ticket.getPrice().multiply(BigDecimal.valueOf(quantity));
        this.purchaseTimestamp = LocalDateTime.now();
        this.orderStatus = PENDING;

        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.buyerEmail = buyerEmail;
    }

    public void cancelOrder() {
        this.orderStatus = CANCELLED;
    }

    public void completeOrder() {
        this.orderStatus = COMPLETED;
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

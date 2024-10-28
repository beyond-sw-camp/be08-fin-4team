package com.beyond.easycheck.tickets.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionAgreementType collectionAgreement;

    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime purchaseTimestamp;

    @OneToMany(mappedBy = "ticketOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderDetailsEntity> orderDetails = new ArrayList<>();

    public TicketOrderEntity(UserEntity userEntity, CollectionAgreementType collectionAgreement) {
        this.userEntity = userEntity;
        this.collectionAgreement = collectionAgreement;
        this.totalPrice = ticket.getPrice().multiply(BigDecimal.valueOf(Long.parseLong(String.valueOf(quantity))));
        this.purchaseTimestamp = LocalDateTime.now();
        this.orderStatus = PENDING;
    }

    public void addOrderDetail(TicketEntity ticket, int quantity) {
        OrderDetailsEntity orderDetail = new OrderDetailsEntity(this, ticket, quantity);
        orderDetails.add(orderDetail);
        this.totalPrice = this.totalPrice.add(orderDetail.getPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity())));
    }

    public void cancelOrder() {
        this.orderStatus = CANCELLED;
    }

    public void completeOrder() {
        this.orderStatus = COMPLETED;
    }
}

package com.beyond.easycheck.payments.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Builder
@Table(name = "Payment")
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    @JsonManagedReference
    private ReservationRoomEntity reservationRoomEntity;

    @Column(nullable = false)
    private String impUid;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = true)
    private String bank;

    @Column(nullable = true)
    private String accountHolder;

    @Column(nullable = true)
    private LocalDateTime depositDeadline;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    private LocalDateTime cancelDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompletionStatus completionStatus;

    public void updateCompletionStatus(CompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }
}


package com.beyond.easycheck.payments.ui.view;

import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PaymentView {

    private Long id;

    private String impUid;

    private Long reservationRoomId;

    private Long userId;

    private int point;

    private LocalDateTime checkinDate;

    private LocalDateTime checkoutDate;

    private LocalDateTime paymentDate;

    private String method;

    private Integer amount;

    private CompletionStatus completionStatus;

    private String accommodationName;

    public static PaymentView of(PaymentEntity paymentEntity) {

        return new PaymentView(

                paymentEntity.getId(),
                paymentEntity.getImpUid(),
                paymentEntity.getReservationRoomEntity().getId(),
                paymentEntity.getReservationRoomEntity().getUserEntity().getId(),
                paymentEntity.getReservationRoomEntity().getUserEntity().getPoint(),
                paymentEntity.getReservationRoomEntity().getCheckinDate().atStartOfDay(),
                paymentEntity.getReservationRoomEntity().getCheckoutDate().atStartOfDay(),
                paymentEntity.getPaymentDate(),
                paymentEntity.getMethod(),
                paymentEntity.getAmount(),
                paymentEntity.getCompletionStatus(),
                paymentEntity.getReservationRoomEntity().getRoomEntity().getRoomTypeEntity().getAccommodationEntity().getName()
        );
    }
}


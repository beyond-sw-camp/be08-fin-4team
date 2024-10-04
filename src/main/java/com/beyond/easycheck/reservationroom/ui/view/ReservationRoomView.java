package com.beyond.easycheck.reservationroom.ui.view;

import com.beyond.easycheck.reservationroom.infrastructure.entity.PaymentStatus;
import com.beyond.easycheck.reservationroom.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationroom.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReservationRoomView {

    private Long id;

    private String userName;

    private Long roomId;

    private String typeName;

    private RoomStatus roomStatus;

    private LocalDateTime checkinDate;

    private LocalDateTime checkoutDate;

    private ReservationStatus reservationStatus;

    private Integer totalPrice;

    private PaymentStatus paymentStatus;

    public static ReservationRoomView of(ReservationRoomEntity reservationRoomEntity) {

        return new ReservationRoomView(

                reservationRoomEntity.getId(),
                reservationRoomEntity.getUserEntity().getName(),
                reservationRoomEntity.getRoomEntity().getRoomId(),
                reservationRoomEntity.getRoomEntity().getRoomTypeEntity().getTypeName(),
                reservationRoomEntity.getRoomEntity().getStatus(),
                reservationRoomEntity.getCheckinDate().toLocalDate().atStartOfDay(),
                reservationRoomEntity.getCheckoutDate().toLocalDate().atStartOfDay(),
                reservationRoomEntity.getReservationStatus(),
                reservationRoomEntity.getTotalPrice(),
                reservationRoomEntity.getPaymentStatus()
        );
    }
}

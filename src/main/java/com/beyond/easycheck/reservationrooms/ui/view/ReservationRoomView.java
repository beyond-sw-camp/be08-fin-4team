package com.beyond.easycheck.reservationrooms.ui.view;

import com.beyond.easycheck.reservationrooms.infrastructure.entity.PaymentStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReservationRoomView {

    private Long id;

    private String userName;

    private Long roomId;

    private String typeName;

    private List<String> imageUrls;

    private RoomStatus roomStatus;

    private LocalDate checkinDate;

    private LocalDate checkoutDate;

    private ReservationStatus reservationStatus;

    private Integer totalPrice;

    private PaymentStatus paymentStatus;

    public static ReservationRoomView of(ReservationRoomEntity reservationRoomEntity) {

        List<String> imageUrls = reservationRoomEntity.getRoomEntity().getImages().stream()
                .map(RoomEntity.ImageEntity::getUrl)
                .collect(Collectors.toList());

        return new ReservationRoomView(

                reservationRoomEntity.getId(),
                reservationRoomEntity.getUserEntity().getName(),
                reservationRoomEntity.getRoomEntity().getRoomId(),
                reservationRoomEntity.getRoomEntity().getRoomTypeEntity().getName(),
                imageUrls,
                reservationRoomEntity.getRoomEntity().getStatus(),
                reservationRoomEntity.getCheckinDate(),
                reservationRoomEntity.getCheckoutDate(),
                reservationRoomEntity.getReservationStatus(),
                reservationRoomEntity.getTotalPrice(),
                reservationRoomEntity.getPaymentStatus()
        );
    }
}

package com.beyond.easycheck.reservationrooms.ui.view;

import com.beyond.easycheck.reservationrooms.infrastructure.entity.PaymentStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationRoomView {

    private Long id;

    private Long userId;

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

    /**
     * 추가 부분
     */
    // 객실 이름
    private String roomName;

    // 대표 투숙자 정보
    private String representativeName;
    private String representativePhone;
    private String representativeEmail;

    // 인원수 정보
    private int adultCount;
    private int childCount;

    // 방 개수
    private int totalRoomCount;

    public static ReservationRoomView of(ReservationRoomEntity reservation) {

        List<String> imageUrls = reservation.getRoomEntity().getImages().stream()
                .map(RoomEntity.ImageEntity::getUrl)
                .collect(Collectors.toList());

        return new ReservationRoomView(
                reservation.getId(),
                reservation.getUserEntity().getId(),
                reservation.getUserEntity().getName(),
                reservation.getRoomEntity().getRoomId(),
                reservation.getRoomEntity().getRoomTypeEntity().getName(),
                imageUrls,
                reservation.getRoomEntity().getStatus(),
                reservation.getCheckinDate(),
                reservation.getCheckoutDate(),
                reservation.getReservationStatus(),
                reservation.getTotalPrice(),
                reservation.getPaymentStatus(),
                reservation.getRoomEntity().getType(),
                reservation.getRepresentativeName(),
                reservation.getRepresentativePhone(),
                reservation.getRepresentativeEmail(),
                reservation.getAdultCount(),
                reservation.getChildCount(),
                reservation.getTotalRoomCount()
        );
    }
}

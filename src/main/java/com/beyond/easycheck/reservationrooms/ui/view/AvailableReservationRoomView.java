package com.beyond.easycheck.reservationrooms.ui.view;

import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableReservationRoomView {
    private Long roomId;
    private String roomType;
    private String roomName;
    private int remainingRoom;
    private RoomStatus status;

    private String description;
    private String thumbnailImgUrl;

    private BigDecimal normalPrice;
    private BigDecimal corpPrice;

    private int maxOccupancy;
    private int standardOccupancy;

    /**
     * 변경사항
     * 가장 비싼 시즌 금액
     * 현재 시즌 금액
     */
    private int expensiveSeasonPrice;
    private int currentSeasonPrice;

}

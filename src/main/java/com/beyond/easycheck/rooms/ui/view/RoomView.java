package com.beyond.easycheck.rooms.ui.view;

import com.beyond.easycheck.rooms.application.dto.FindRoomResult;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomView {

    private Long roomId;

    private String type;

    private List<String> images;

    private int roomAmount;

    private int remainingRoom;

    private RoomStatus status;

    private Long roomTypeId;

    private Long accommodationId;

    private String description;

    private String checkInTime;

    private String checkOutTime;

    private String composition;

    private String size;

    private int maxOccupancy;

    private int standardOccupancy;

    public RoomView(FindRoomResult result) {
        this.roomId = result.roomId();
        this.type = result.type();
        this.images = result.images();
        this.roomAmount = result.roomAmount();
        this.remainingRoom = result.remainingRoom();
        this.status = result.status();
        this.description = result.description();
        this.checkInTime = result.checkInTime();
        this.checkOutTime = result.checkOutTime();
        this.composition = result.composition();
        this.size = result.size();
        this.maxOccupancy = result.maxOccupancy();
        this.standardOccupancy = result.standardOccupancy();

        // roomTypeId랑 accommodationId 필요??
    }

}

package com.beyond.easycheck.rooms.ui.requestbody;

import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateRequest {

    @NotNull
    private Long roomTypeId;

    @NotNull
    private String type;

    @NotNull
    private RoomStatus status;

    @NotNull @Max(10)
    private int roomAmount;

    @NotNull @Max(10)
    private int remainingRoom;

}

package com.beyond.easycheck.rooms.ui.requestbody;

import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {

    @NotNull
    private Long roomtypeEntity;

    @NotBlank
    private String roomNumber;

    @NotNull @Max(10)
    private int roomAmount;

    @NotBlank
    private RoomStatus status;

}

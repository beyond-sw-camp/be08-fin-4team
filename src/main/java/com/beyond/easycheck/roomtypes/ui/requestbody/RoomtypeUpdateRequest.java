package com.beyond.easycheck.roomtypes.ui.requestbody;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RoomtypeUpdateRequest {

    @NotBlank
    private String typeName;

}

package com.beyond.easycheck.roomtypes.ui.view;

import com.beyond.easycheck.roomtypes.application.dto.FindRoomTypeResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomtypeView {

    private Long roomTypeId;

    private String name;

    private String description;

    private String thumbnailUrl;

    public RoomtypeView(FindRoomTypeResult result) {
        this.roomTypeId = result.roomTypeId();
        this.name = result.name();
        this.description = result.description();
        this.thumbnailUrl = result.thumbnailUrl();
    }
}

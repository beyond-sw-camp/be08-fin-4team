package com.beyond.easycheck.roomtypes.ui.requestbody;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RoomtypeCreateRequest {

    @NotNull
    private Long accommodationId;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String thumbnailUrl;
}

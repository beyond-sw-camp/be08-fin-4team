package com.beyond.easycheck.rooms.application.dto;

public record RoomFindQuery(
        Long accommodationId,
        Long roomTypeId
) {
}

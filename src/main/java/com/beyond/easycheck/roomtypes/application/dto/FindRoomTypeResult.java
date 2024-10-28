package com.beyond.easycheck.roomtypes.application.dto;

import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;

public record FindRoomTypeResult(
        Long roomTypeId,
        String name,
        String description,
        String thumbnailUrl
) {
    public static FindRoomTypeResult findByRoomTypeEntity(RoomtypeEntity roomTypeEntity) {
        return new FindRoomTypeResult(
                roomTypeEntity.getRoomTypeId(),
                roomTypeEntity.getName(),
                roomTypeEntity.getDescription(),
                roomTypeEntity.getThumbnailUrl()
        );
    }
}

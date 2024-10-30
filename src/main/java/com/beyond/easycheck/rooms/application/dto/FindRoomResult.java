package com.beyond.easycheck.rooms.application.dto;

import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity.ImageEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;

import java.util.List;

public record FindRoomResult(
        Long roomId,
        String type,
        RoomStatus status,
        int roomAmount,
        int remainingRoom,
        List<String> images,
        // roomType 관련
        // room details
        String description,
        String checkInTime,
        String checkOutTime,
        String composition,
        String size,
        int maxOccupancy,
        int standardOccupancy
) {

    public static FindRoomResult findByRoomEntity(RoomEntity room) {
        return new FindRoomResult(
                room.getRoomId(),
                room.getType(),
                room.getStatus(),
                room.getRoomAmount(),
                room.getRemainingRoom(),
                // image url 받아오기
                room.getImages().stream()
                        .map(ImageEntity::getUrl)
                        .toList(),
                room.getDescription(),
                room.getCheckInTime(),
                room.getCheckOutTime(),
                room.getComposition(),
                room.getSize(),
                room.getMaxOccupancy(),
                room.getStandardOccupancy()
        );
    }
}

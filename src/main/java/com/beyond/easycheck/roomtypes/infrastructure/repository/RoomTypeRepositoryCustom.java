package com.beyond.easycheck.roomtypes.infrastructure.repository;

import com.beyond.easycheck.roomtypes.application.dto.RoomTypeFindQuery;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;

import java.util.List;

public interface RoomTypeRepositoryCustom {
    List<RoomtypeEntity> findAllRoomTypes(RoomTypeFindQuery query);
}

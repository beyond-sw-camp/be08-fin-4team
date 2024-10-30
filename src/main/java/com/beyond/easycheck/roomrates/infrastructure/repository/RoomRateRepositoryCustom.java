package com.beyond.easycheck.roomrates.infrastructure.repository;

import com.beyond.easycheck.roomrates.application.dto.RoomRateFindQuery;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;

import java.util.List;

public interface RoomRateRepositoryCustom {

    List<RoomRateEntity> findAllRoomRates(RoomRateFindQuery query);
}

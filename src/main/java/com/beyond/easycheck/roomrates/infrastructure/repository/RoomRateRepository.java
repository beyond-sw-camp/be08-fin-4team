package com.beyond.easycheck.roomrates.infrastructure.repository;

import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRateRepository extends JpaRepository<RoomRateEntity, Long>, RoomRateRepositoryCustom{

    Optional<RoomRateEntity> findByRoomEntityAndUserType(RoomEntity roomEntity, String userType);

}

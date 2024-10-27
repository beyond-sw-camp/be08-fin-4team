package com.beyond.easycheck.roomtypes.infrastructure.repository;

import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomtypeRepository extends JpaRepository<RoomtypeEntity, Long>, RoomTypeRepositoryCustom {
}

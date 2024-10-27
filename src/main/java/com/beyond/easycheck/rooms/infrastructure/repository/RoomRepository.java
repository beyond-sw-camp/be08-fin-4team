package com.beyond.easycheck.rooms.infrastructure.repository;

import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, Long>, RoomRepositoryCustom {

    List<RoomEntity> findByRoomTypeEntity_RoomTypeId(Long roomTypeId);
}

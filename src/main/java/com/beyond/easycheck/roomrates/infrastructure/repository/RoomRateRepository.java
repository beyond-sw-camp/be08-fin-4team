package com.beyond.easycheck.roomrates.infrastructure.repository;

import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRateRepository extends JpaRepository<RoomRateEntity, Long>, RoomRateRepositoryCustom{
}

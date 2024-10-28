package com.beyond.easycheck.events.infrastructure.repository;

import com.beyond.easycheck.events.infrastructure.entity.EventEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long>, EventRepositoryCustom {

    @EntityGraph(attributePaths = {"accommodationEntity"})
    List<EventEntity> findAllByAccommodationEntity_Id(Long accommodationId);

}

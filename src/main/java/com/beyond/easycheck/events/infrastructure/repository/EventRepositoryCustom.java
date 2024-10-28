package com.beyond.easycheck.events.infrastructure.repository;

import com.beyond.easycheck.events.application.service.dto.EventFindQuery;
import com.beyond.easycheck.events.infrastructure.entity.EventEntity;

import java.util.List;

public interface EventRepositoryCustom {

    List<EventEntity> findAllEvents(EventFindQuery query);
}

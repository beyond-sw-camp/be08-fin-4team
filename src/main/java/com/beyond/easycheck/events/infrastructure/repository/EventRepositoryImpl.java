package com.beyond.easycheck.events.infrastructure.repository;

import com.beyond.easycheck.accomodations.infrastructure.entity.QAccommodationEntity;
import com.beyond.easycheck.events.application.service.dto.EventFindQuery;
import com.beyond.easycheck.events.infrastructure.entity.EventEntity;
import com.beyond.easycheck.events.infrastructure.entity.QEventEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventEntity> findAllEvents(EventFindQuery query) {
        QAccommodationEntity accommodation = QAccommodationEntity.accommodationEntity;
        QEventEntity event = QEventEntity.eventEntity;

        return queryFactory
                .selectFrom(event)
                .rightJoin(event.accommodationEntity, accommodation).fetchJoin()
                .where(
                        accommodationIdEq(query.accommodationId())
                )
                .distinct()
                .fetch();

    }

    private BooleanExpression accommodationIdEq(Long accommodationId) {
        return accommodationId != null ?
                QEventEntity.eventEntity.accommodationEntity.id.eq(accommodationId) : null;
    }
}

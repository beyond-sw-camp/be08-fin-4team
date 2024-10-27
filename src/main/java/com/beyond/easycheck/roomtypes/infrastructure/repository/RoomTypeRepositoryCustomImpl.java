package com.beyond.easycheck.roomtypes.infrastructure.repository;

import com.beyond.easycheck.accomodations.infrastructure.entity.QAccommodationEntity;
import com.beyond.easycheck.roomtypes.application.dto.RoomTypeFindQuery;
import com.beyond.easycheck.roomtypes.infrastructure.entity.QRoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RoomTypeRepositoryCustomImpl implements RoomTypeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomtypeEntity> findAllRoomTypes(RoomTypeFindQuery query) {
        QRoomtypeEntity roomType = QRoomtypeEntity.roomtypeEntity;
        QAccommodationEntity accommodation = QAccommodationEntity.accommodationEntity;

        return queryFactory.selectFrom(roomType)
                .join(roomType.accommodationEntity, accommodation)
                .where(accommodationIdEq(query.accommodationId()))
                .fetch();
    }

    // roomTypeId 조건 메서드 추가
    private BooleanExpression accommodationIdEq(Long accommodationId) {
        return accommodationId != null ?
                QRoomtypeEntity.roomtypeEntity.accommodationEntity.id.eq(accommodationId) : null;
    }
}

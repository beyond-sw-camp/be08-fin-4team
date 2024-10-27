package com.beyond.easycheck.rooms.infrastructure.repository;

import com.beyond.easycheck.accomodations.infrastructure.entity.QAccommodationEntity;
import com.beyond.easycheck.rooms.application.dto.RoomFindQuery;
import com.beyond.easycheck.rooms.infrastructure.entity.QRoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.roomtypes.infrastructure.entity.QRoomtypeEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomEntity> findAllRooms(RoomFindQuery query) {
        QRoomEntity room = QRoomEntity.roomEntity;
        QRoomtypeEntity roomType = QRoomtypeEntity.roomtypeEntity;
        QAccommodationEntity accommodation = QAccommodationEntity.accommodationEntity;

        return queryFactory
                .selectFrom(room)
                .join(room.roomTypeEntity, roomType).fetchJoin()
                .join(roomType.accommodationEntity, accommodation).fetchJoin()
                .leftJoin(room.images).fetchJoin()  // 이미지도 함께 조회
                .where(
                        accommodationIdEq(query.accommodationId()),
                        roomTypeIdEq(query.roomTypeId())  // 룸 타입 ID 조건 추가
                )
                .distinct()  // 이미지 때문에 중복 제거
                .fetch();
    }

    // roomTypeId 조건 메서드 추가
    private BooleanExpression roomTypeIdEq(Long roomTypeId) {
        return roomTypeId != null ?
                QRoomEntity.roomEntity.roomTypeEntity.roomTypeId.eq(roomTypeId) : null;
    }

    private BooleanExpression accommodationIdEq(Long accommodationId) {
        return accommodationId != null ?
                QRoomtypeEntity.roomtypeEntity.accommodationEntity.id.eq(accommodationId) : null;
    }

    private BooleanExpression roomNumberContains(String roomNumber) {
        return roomNumber != null ?
                QRoomEntity.roomEntity.type.contains(roomNumber) : null;
    }

    private BooleanExpression statusEq(RoomStatus status) {
        return status != null ?
                QRoomEntity.roomEntity.status.eq(status) : null;
    }

    private BooleanExpression roomAmountGoe(Integer minAmount) {
        return minAmount != null ?
                QRoomEntity.roomEntity.roomAmount.goe(minAmount) : null;
    }
}
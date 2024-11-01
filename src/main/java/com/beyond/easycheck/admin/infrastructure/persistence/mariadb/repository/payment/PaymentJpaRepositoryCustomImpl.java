package com.beyond.easycheck.admin.infrastructure.persistence.mariadb.repository.payment;

import com.beyond.easycheck.admin.application.service.AdminReadUseCase.PaymentFindQuery;
import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import com.beyond.easycheck.payments.infrastructure.entity.QPaymentEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.QReservationRoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.QRoomEntity;
import com.beyond.easycheck.roomtypes.infrastructure.entity.QRoomtypeEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.QAccommodationEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.QUserEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PaymentJpaRepositoryCustomImpl implements PaymentJpaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PaymentEntity> findAllPayments(Long managedAccommodationId, PaymentFindQuery query) {
        QPaymentEntity payment = QPaymentEntity.paymentEntity;
        QReservationRoomEntity reservationRoom = QReservationRoomEntity.reservationRoomEntity;
        QRoomEntity room = QRoomEntity.roomEntity;
        QRoomtypeEntity roomType = QRoomtypeEntity.roomtypeEntity;
        QAccommodationEntity accommodation = QAccommodationEntity.accommodationEntity;
        QUserEntity user = QUserEntity.userEntity;

        // 동적 쿼리 조건 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 필수 조건: 관리자가 담당하는 숙소의 결제 내역만 조회
        builder.and(roomType.accommodationEntity.id.eq(managedAccommodationId));

        // 선택적 조건들 추가 - 부분 일치 검색으로 변경
        if (StringUtils.hasText(query.userName())) {
            builder.and(reservationRoom.userEntity.name.contains(query.userName()));
        }

        if (StringUtils.hasText(query.email())) {
            builder.and(reservationRoom.userEntity.email.contains(query.email()));
        }

        // 결과 조회 쿼리
        return queryFactory
                .selectFrom(payment)
                .join(payment.reservationRoomEntity, reservationRoom)
                .join(reservationRoom.roomEntity, room)
                .join(room.roomTypeEntity, roomType)
                .join(roomType.accommodationEntity, accommodation)
                .join(reservationRoom.userEntity, user)
                .where(builder)
                .orderBy(payment.paymentDate.desc())
                .fetch();
    }
}
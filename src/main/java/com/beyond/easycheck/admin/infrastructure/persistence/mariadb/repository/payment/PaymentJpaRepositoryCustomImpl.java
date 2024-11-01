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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PaymentJpaRepositoryCustomImpl implements PaymentJpaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PaymentEntity> findAllPayments(Long managedAccommodationId, PaymentFindQuery query, Pageable pageable) {
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

        // 선택적 조건들 추가
        if (query.paymentId() != null) {
            builder.and(payment.id.eq(query.paymentId()));
        }

        if (StringUtils.hasText(query.userName())) {
            builder.and(reservationRoom.userEntity.name.containsIgnoreCase(query.userName()));
        }

        if (StringUtils.hasText(query.email())) {
            builder.and(reservationRoom.userEntity.email.eq(query.email()));
        }

        // 결과 조회 쿼리
        List<PaymentEntity> content = queryFactory
                .selectFrom(payment)
                .join(payment.reservationRoomEntity, reservationRoom)
                .join(reservationRoom.roomEntity, room)
                .join(room.roomTypeEntity, roomType)
                .join(roomType.accommodationEntity, accommodation)
                .join(reservationRoom.userEntity, user)
                .where(builder)
                .orderBy(payment.paymentDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(payment.count())
                .from(payment)
                .join(payment.reservationRoomEntity, reservationRoom)
                .join(reservationRoom.roomEntity, room)
                .join(room.roomTypeEntity, roomType)
                .join(roomType.accommodationEntity, accommodation)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression paymentIdEq(Long paymentId) {
        return paymentId != null ? QPaymentEntity.paymentEntity.id.eq(paymentId) : null;
    }

    private BooleanExpression userNameContains(String userName) {
        QUserEntity user = QUserEntity.userEntity;
        return StringUtils.hasText(userName) ?
                user.name.containsIgnoreCase(userName) : null;
    }

    private BooleanExpression emailEq(String email) {
        QUserEntity user = QUserEntity.userEntity;
        return StringUtils.hasText(email) ?
                user.email.eq(email) : null;
    }
}
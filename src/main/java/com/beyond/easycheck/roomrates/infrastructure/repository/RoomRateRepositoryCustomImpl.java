package com.beyond.easycheck.roomrates.infrastructure.repository;

import com.beyond.easycheck.roomrates.application.dto.RoomRateFindQuery;
import com.beyond.easycheck.roomrates.infrastructure.entity.QRoomRateEntity;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;
import com.beyond.easycheck.seasons.infrastructure.entity.QSeasonEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
public class RoomRateRepositoryCustomImpl implements RoomRateRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomRateEntity> findAllRoomRates(RoomRateFindQuery query) {
        QRoomRateEntity roomRate = QRoomRateEntity.roomRateEntity;
        QSeasonEntity season = QSeasonEntity.seasonEntity;

        // 계절 정렬 (봄부터 시작)
        NumberExpression<Integer> seasonOrder = new CaseBuilder()
                .when(season.seasonName.startsWith("봄")).then(1)
                .when(season.seasonName.startsWith("여름")).then(2)
                .when(season.seasonName.startsWith("가을")).then(3)
                .when(season.seasonName.startsWith("겨울")).then(4)
                .otherwise(5);

        // 주간/주말 순서
        NumberExpression<Integer> dayTypeOrder = new CaseBuilder()
                .when(season.seasonName.contains("주간")).then(1)
                .when(season.seasonName.contains("주말")).then(2)
                .otherwise(3);

        // 비수기/성수기 순서
        NumberExpression<Integer> peakOrder = new CaseBuilder()
                .when(season.seasonName.contains("비수기")).then(1)
                .when(season.seasonName.contains("성수기")).then(2)
                .otherwise(3);

        return queryFactory
                .selectFrom(roomRate)
                .join(roomRate.seasonEntity, season).fetchJoin()
                .where(
                        roomIdEq(query.roomId()),
                        isDateBetweenSeasonPeriod(query.seasonStartDate())
                )
                .orderBy(
                        seasonOrder.asc(),
                        dayTypeOrder.asc(),
                        peakOrder.asc()
                )
                .fetch();
    }

    private BooleanExpression roomIdEq(Long roomId) {
        return roomId != null ?
                QRoomRateEntity.roomRateEntity.roomEntity.roomId.eq(roomId) : null;
    }

    private BooleanExpression isDateBetweenSeasonPeriod(LocalDate date) {
        if (date == null) {
            return null;
        }

        QSeasonEntity season = QSeasonEntity.seasonEntity;
        return season.startDate.loe(date)  // less than or equal (날짜가 시작일보다 같거나 이후)
                .and(season.endDate.goe(date));  // greater than or equal (날짜가 종료일보다 같거나 이전)
    }
}
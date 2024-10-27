package com.beyond.easycheck.roomrates.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomrates.application.dto.RoomRateFindQuery;
import com.beyond.easycheck.roomrates.exception.RoomrateMessageType;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;
import com.beyond.easycheck.roomrates.infrastructure.repository.RoomRateRepository;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateCreateRequest;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateUpdateRequest;
import com.beyond.easycheck.roomrates.ui.view.RoomRateView;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomtypeRepository;
import com.beyond.easycheck.seasons.infrastructure.entity.SeasonEntity;
import com.beyond.easycheck.seasons.infrastructure.repository.SeasonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.beyond.easycheck.roomrates.exception.RoomrateMessageType.*;
import static com.beyond.easycheck.rooms.exception.RoomMessageType.ROOM_NOT_FOUND;
import static com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType.ROOM_TYPE_NOT_FOUND;
import static com.beyond.easycheck.seasons.exception.SeasonMessageType.SEASON_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RoomRateService {

    private final RoomRateRepository roomrateRepository;
    private final RoomRepository roomRepository;
    private final SeasonRepository seasonRepository;
    private final RoomtypeRepository roomTypeRepository;

    @Transactional
    public void createRoomRate(RoomrateCreateRequest roomrateCreateRequest) {

        RoomEntity room = roomRepository.findById(roomrateCreateRequest.getRoomEntity())
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        SeasonEntity season = seasonRepository.findById(roomrateCreateRequest.getSeasonEntity())
                .orElseThrow(() -> new EasyCheckException(SEASON_NOT_FOUND));

        if (roomrateCreateRequest.getRateType() == null || roomrateCreateRequest.getRate() == null) {
            throw new EasyCheckException(RoomrateMessageType.ARGUMENT_NOT_VALID);
        }

        RoomRateEntity roomrate = RoomRateEntity.builder()
                .roomEntity(room)
                .seasonEntity(season)
                .rateType(roomrateCreateRequest.getRateType())
                .rate(roomrateCreateRequest.getRate())
                .build();

       roomrateRepository.save(roomrate);
    }

    public RoomRateView readRoomRate(Long id) {

        RoomRateEntity roomrate = roomrateRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(ROOM_RATE_NOT_FOUND));

        RoomEntity room = roomRepository.findById(roomrate.getRoomEntity().getRoomId())
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        SeasonEntity season = seasonRepository.findById(roomrate.getSeasonEntity().getId())
                .orElseThrow(() -> new EasyCheckException(SEASON_NOT_FOUND));

        RoomtypeEntity roomtype = roomTypeRepository.findById(roomrate.getRoomEntity().getRoomTypeEntity().getRoomTypeId())
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

//        RoomRateView roomrateView = RoomRateView.builder()
//                .id(roomrate.getId())
//                .rateType(roomrate.getRateType())
//                .rate(roomrate.getRate())
//                .seasonName(season.getSeasonName())
//                .build();

        return null;
    }

    public List<RoomRateView> readRoomRates(RoomRateFindQuery query) {
        List<RoomRateEntity> roomRateEntities = roomrateRepository.findAllRoomRates(query);

        // 먼저 combine된 RoomRateView 리스트를 만든 후 정렬
        List<RoomRateView> roomRateViews = roomRateEntities.stream()
                .collect(Collectors.groupingBy(
                        roomRateEntity -> roomRateEntity.getSeasonEntity().getSeasonName(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    String seasonName = entry.getKey();
                    List<RoomRateEntity> rates = entry.getValue();

                    BigDecimal normalRate = rates.stream()
                            .filter(rate -> "일반".equals(rate.getUserType()))
                            .findFirst()
                            .map(RoomRateEntity::getRate)
                            .orElse(BigDecimal.ZERO);

                    BigDecimal corpRate = rates.stream()
                            .filter(rate -> "법인".equals(rate.getUserType()))
                            .findFirst()
                            .map(RoomRateEntity::getRate)
                            .orElse(BigDecimal.ZERO);

                    return RoomRateView.builder()
                            .id(rates.get(0).getId())
                            .normalRate(normalRate)
                            .corpRate(corpRate)
                            .seasonName(seasonName)
                            .build();
                })
                .collect(Collectors.toList());

        // 정렬 로직
        Comparator<RoomRateView> viewComparator = (v1, v2) -> {
            String[] seasons = {"봄", "여름", "가을", "겨울"};
            String season1 = v1.getSeasonName().split(" ")[0];  // "봄", "여름" 등을 추출
            String season2 = v2.getSeasonName().split(" ")[0];

            // 계절 순서 비교
            int seasonCompare = Integer.compare(
                    Arrays.asList(seasons).indexOf(season1),
                    Arrays.asList(seasons).indexOf(season2)
            );
            if (seasonCompare != 0) return seasonCompare;

            // 주간/주말 순서 비교
            boolean isDay1 = v1.getSeasonName().contains("주간");
            boolean isDay2 = v2.getSeasonName().contains("주간");
            if (isDay1 != isDay2) return isDay1 ? -1 : 1;

            // 비수기/성수기 순서 비교
            boolean isPeak1 = v1.getSeasonName().contains("비수기");
            boolean isPeak2 = v2.getSeasonName().contains("비수기");
            return isPeak1 ? -1 : 1;
        };
        // 정렬 적용
        roomRateViews.sort(viewComparator);

        return roomRateViews;
    }
    @Transactional
    public void updateRoomRate(Long roomrateId, RoomrateUpdateRequest roomrateUpdateRequest) {

        RoomRateEntity roomrate = roomrateRepository.findById(roomrateId)
                .orElseThrow(() -> new EasyCheckException(ROOM_RATE_NOT_FOUND));

        RoomEntity roomEntity = roomRepository.findById(roomrateUpdateRequest.getRoomEntity())
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        SeasonEntity seasonEntity = seasonRepository.findById(roomrateUpdateRequest.getSeasonEntity())
                .orElseThrow(() -> new EasyCheckException(SEASON_NOT_FOUND));

        if(roomrateUpdateRequest.getRateType() == null || roomrateUpdateRequest.getRate() == null) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }

        roomrate.update(roomrateUpdateRequest, roomEntity, seasonEntity);
        roomrateRepository.save(roomrate);
    }

    @Transactional
    public void deleteRoomRate(Long roomrateId) {
        RoomRateEntity roomrate = roomrateRepository.findById(roomrateId)
                .orElseThrow(() -> new EasyCheckException(ROOM_RATE_NOT_FOUND));

        roomrateRepository.delete(roomrate);
    }

}

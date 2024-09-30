package com.beyond.easycheck.roomrates.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomrateEntity;
import com.beyond.easycheck.roomrates.infrastructure.repository.RoomrateRepository;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateCreateRequest;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateUpdateRequest;
import com.beyond.easycheck.roomrates.ui.view.RoomrateView;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomTypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomTypeRepository;
import com.beyond.easycheck.seasons.infrastructure.entity.SeasonEntity;
import com.beyond.easycheck.seasons.infrastructure.repository.SeasonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.beyond.easycheck.roomrates.exception.RoomrateMessageType.ROOM_RATE_NOT_FOUND;
import static com.beyond.easycheck.rooms.exception.RoomMessageType.ROOM_NOT_FOUND;
import static com.beyond.easycheck.roomtypes.exception.RoomTypeMessageType.ROOM_TYPE_NOT_FOUND;
import static com.beyond.easycheck.seasons.exception.SeasonMessageType.SEASON_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RoomrateService {

    private final RoomrateRepository roomrateRepository;
    private final RoomRepository roomRepository;
    private final SeasonRepository seasonRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Transactional
    public void createRoomrate(RoomrateCreateRequest roomrateCreateRequest) {

        RoomEntity room = roomRepository.findById(roomrateCreateRequest.getRoomEntity())
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        SeasonEntity season = seasonRepository.findById(roomrateCreateRequest.getSeasonEntity())
                .orElseThrow(() -> new EasyCheckException(SEASON_NOT_FOUND));

        RoomrateEntity roomrate = RoomrateEntity.builder()
                .roomEntity(room)
                .seasonEntity(season)
                .rateType(roomrateCreateRequest.getRateType())
                .rate(roomrateCreateRequest.getRate())
                .build();

        roomrate = roomrateRepository.save(roomrate);
    }

    public RoomrateView readRoomrate(Long id) {

        RoomrateEntity roomrate = roomrateRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(ROOM_RATE_NOT_FOUND));

        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        SeasonEntity season = seasonRepository.findById(roomrate.getSeasonEntity().getId())
                .orElseThrow(() -> new EasyCheckException(SEASON_NOT_FOUND));

        RoomTypeEntity roomtype = roomTypeRepository.findById(roomrate.getRoomEntity().getRoomTypeEntity().getRoomTypeId())
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        RoomrateView roomrateView = RoomrateView.builder()
                .id(roomrate.getId())
                .rateType(roomrate.getRateType())
                .rate(roomrate.getRate())
                .status(room.getStatus())
                .typeName(roomtype.getTypeName())
                .seasonName(season.getSeasonName())
                .build();

        return roomrateView;
    }

    @Transactional
    public List<RoomrateView> readRoomrates() {

        List<RoomrateEntity> roomrateEntities = roomrateRepository.findAll();

        if (roomrateEntities.isEmpty()) {
            throw new EasyCheckException(ROOM_RATE_NOT_FOUND);
        }
        List<RoomrateView> roomrateViews = roomrateEntities.stream()
                .map(roomrateEntity -> new RoomrateView(
                        roomrateEntity.getId(),
                        roomrateEntity.getRateType(),
                        roomrateEntity.getRate(),
                        roomrateEntity.getRoomEntity().getStatus(),
                        roomrateEntity.getRoomEntity().getRoomTypeEntity().getTypeName(),
                        roomrateEntity.getSeasonEntity().getSeasonName()
                )).collect(Collectors.toList());

        return roomrateViews;
    }

    @Transactional
    public void updateRoomrate(Long roomrateId, RoomrateUpdateRequest roomrateUpdateRequest) {

        RoomrateEntity roomrate = roomrateRepository.findById(roomrateId)
                .orElseThrow(() -> new EasyCheckException(ROOM_RATE_NOT_FOUND));

        RoomEntity roomEntity = roomRepository.findById(roomrateUpdateRequest.getRoomEntity())
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        SeasonEntity seasonEntity = seasonRepository.findById(roomrateUpdateRequest.getSeasonEntity())
                .orElseThrow(() -> new EasyCheckException(SEASON_NOT_FOUND));

        roomrate.update(roomrateUpdateRequest, roomEntity, seasonEntity);

        roomrateRepository.save(roomrate);
    }

    @Transactional
    public void deleteRoomrate(Long roomrateId) {
        RoomrateEntity roomrate = roomrateRepository.findById(roomrateId)
                .orElseThrow(() -> new EasyCheckException(ROOM_RATE_NOT_FOUND));

        roomrateRepository.delete(roomrate);
    }

}

package com.beyond.easycheck.roomtypes.application.service;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomtypes.application.dto.RoomTypeFindQuery;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomtypeRepository;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeCreateRequest;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeUpdateRequest;
import com.beyond.easycheck.roomtypes.ui.view.RoomtypeView;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType.ROOM_TYPE_NOT_FOUND;
import static com.beyond.easycheck.accomodations.exception.AccommodationMessageType.ACCOMMODATION_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoomtypeService {

    private final RoomtypeRepository roomTypeRepository;
    private final AccommodationRepository accommodationRepository;

    @Transactional
    public void createRoomtype(RoomtypeCreateRequest roomTypeCreateRequest) {

        AccommodationEntity accommodationEntity = accommodationRepository.findById(roomTypeCreateRequest.getAccommodationId())
                .orElseThrow(() -> new EasyCheckException(ACCOMMODATION_NOT_FOUND));

        RoomtypeEntity roomType = RoomtypeEntity.builder()
                .accommodationEntity(accommodationEntity)
                .name(roomTypeCreateRequest.getTypeName())
                .build();

        roomTypeRepository.save(roomType);
    }

    @Transactional
    public RoomtypeView readRoomtype(Long roomTypeId) {

        RoomtypeEntity roomTypeEntity = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        AccommodationEntity accommodationEntity = accommodationRepository.findById(roomTypeEntity.getAccommodationEntity().getId())
                .orElseThrow(() -> new EasyCheckException(ACCOMMODATION_NOT_FOUND));

        RoomtypeView roomtypeView = RoomtypeView.builder()
                .accommodationId(accommodationEntity.getId())
                .roomTypeId(roomTypeEntity.getRoomTypeId())
                .name(roomTypeEntity.getName())
                .build();

        return roomtypeView;
    }

    public List<RoomtypeView> readRoomtypes(RoomTypeFindQuery query) {

        List<RoomtypeEntity> roomTypeEntities = roomTypeRepository.findAllRoomTypes(query);

        return roomTypeEntities.stream()
                .map(roomTypeEntity -> new RoomtypeView(
                        roomTypeEntity.getRoomTypeId(),
                        roomTypeEntity.getAccommodationEntity().getId(),
                        roomTypeEntity.getName()
                )).collect(Collectors.toList());
    }

    @Transactional
    public void updateRoomtype(Long roomTypeId, RoomtypeUpdateRequest roomTypeUpdateRequest) {

        RoomtypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        roomType.update(roomTypeUpdateRequest);

    }

    @Transactional
    public void deleteRoomtype(Long roomTypeId) {
        RoomtypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        roomTypeRepository.delete(roomType);
    }
}

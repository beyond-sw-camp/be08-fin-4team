package com.beyond.easycheck.rooms.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.rooms.application.dto.RoomFindQuery;
import com.beyond.easycheck.rooms.infrastructure.entity.DailyRoomAvailabilityEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.DailyRoomAvailabilityRepository;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomImageRepository;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.rooms.ui.requestbody.RoomCreateRequest;
import com.beyond.easycheck.rooms.ui.requestbody.RoomUpdateRequest;
import com.beyond.easycheck.rooms.ui.view.RoomView;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomtypeRepository;
import com.beyond.easycheck.s3.application.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.beyond.easycheck.rooms.exception.RoomMessageType.*;
import static com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType.ROOM_TYPE_NOT_FOUND;
import static com.beyond.easycheck.s3.application.domain.FileManagementCategory.ROOM;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final RoomtypeRepository roomTypeRepository;
    private final RoomImageRepository roomImageRepository;
    private final DailyRoomAvailabilityRepository dailyRoomAvailabilityRepository;
    private final S3Service s3Service;


    private void addImagesToRoom(RoomEntity roomEntity, List<String> imageUrls) {
        List<RoomEntity.ImageEntity> newImageEntities = imageUrls.stream()
                .map(url -> RoomEntity.ImageEntity.createImage(url, roomEntity))
                .toList();

        if (roomEntity.getImages() == null) {
            roomEntity.setImages(new ArrayList<>());
        }

        for (RoomEntity.ImageEntity newImage : newImageEntities) {
            if (!roomEntity.getImages().contains(newImage)) {
                roomEntity.addImage(newImage);
            }
        }
    }

    @Transactional
    public RoomEntity createRoom(RoomCreateRequest roomCreateRequest, List<MultipartFile> imageFiles) {

        RoomtypeEntity roomType = roomTypeRepository.findById(roomCreateRequest.getRoomTypeId())
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        if (roomCreateRequest.getStatus() == null || roomCreateRequest.getRoomNumber() == null ||
                roomCreateRequest.getRemainingRoom() < 0 || roomCreateRequest.getRoomAmount() < 0) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }
        List<String> imageUrls = s3Service.uploadFiles(imageFiles, ROOM);

        RoomEntity room = RoomEntity.builder()
                .roomTypeEntity(roomType)
                .roomNumber(roomCreateRequest.getRoomNumber())
                .status(roomCreateRequest.getStatus())
                .roomAmount(roomCreateRequest.getRoomAmount())
                .remainingRoom(roomCreateRequest.getRemainingRoom())
                .build();

        roomRepository.save(room);
        addImagesToRoom(room, imageUrls);

        return room;
    }

    public void initializeRoomAvailability(RoomEntity roomEntity) {
        LocalDate today = LocalDate.now();

        for (LocalDate date = today; !date.isAfter(today.plusDays(30)); date = date.plusDays(1)) {
            DailyRoomAvailabilityEntity dailyAvailability = dailyRoomAvailabilityRepository
                    .findByRoomEntityAndDate(roomEntity, date.atStartOfDay())
                    .orElse(null);

            if (dailyAvailability == null) {
                dailyAvailability = DailyRoomAvailabilityEntity.builder()
                        .roomEntity(roomEntity)
                        .date(date.atStartOfDay())
                        .remainingRoom(roomEntity.getRoomAmount())
                        .status(RoomStatus.예약가능)
                        .build();

                dailyRoomAvailabilityRepository.save(dailyAvailability);
            }
        }
    }

    public RoomView readRoom(Long id) {

        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        RoomtypeEntity roomType = room.getRoomTypeEntity();

        List<String> images = room.getImages().stream()
                .map(RoomEntity.ImageEntity::getUrl)
                .collect(Collectors.toList());

        return RoomView.builder()
                .roomId(room.getRoomId())
                .images(images)
                .roomNumber(room.getType())
                .roomAmount(room.getRoomAmount())
                .remainingRoom(room.getRemainingRoom())
                .status(room.getStatus())
                .roomTypeId(roomType.getRoomTypeId())
                .accomodationId(roomType.getAccommodationEntity().getId())
                .typeName(roomType.getTypeName())
                .description(roomType.getDescription())
                .maxOccupancy(roomType.getMaxOccupancy())
                .build();
    }

    public List<RoomView> readRooms(RoomFindQuery query) {

        List<RoomEntity> roomEntities = roomRepository.findAllRooms(query);

        if (roomEntities.isEmpty()) {
            throw new EasyCheckException(ROOMS_NOT_FOUND);
        }

        List<RoomView> roomViews = roomEntities.stream()
                .map(roomEntity -> {
                    List<String> imageUrls = roomEntity.getImages().stream()
                            .map(RoomEntity.ImageEntity::getUrl)
                            .collect(Collectors.toList());

                    return new RoomView(
                            roomEntity.getRoomId(),
                            roomEntity.getType(),
                            imageUrls,
                            roomEntity.getRoomAmount(),
                            roomEntity.getRemainingRoom(),
                            roomEntity.getStatus(),
                            roomEntity.getRoomTypeEntity().getRoomTypeId(),
                            roomEntity.getRoomTypeEntity().getAccommodationEntity().getId(),
                            roomEntity.getRoomTypeEntity().getTypeName(),
                            roomEntity.getRoomTypeEntity().getDescription(),
                            roomEntity.getRoomTypeEntity().getMaxOccupancy()
                    );
                })
                .collect(Collectors.toList());

        return roomViews;
    }

    @Transactional
    public void updateRoom(Long roomId, RoomUpdateRequest roomUpdateRequest) {

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        RoomtypeEntity roomtypeEntity = roomTypeRepository.findById(roomUpdateRequest.getRoomtypeEntity())
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        room.update(roomUpdateRequest);
    }

    @Transactional
    public void updateRoomImage(Long imageId, MultipartFile newImageFile) {
        RoomEntity.ImageEntity imageToUpdate = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new EasyCheckException(ROOM_IMAGE_NOT_FOUND));

        String oldImageUrl = imageToUpdate.getUrl();

        String[] parts = oldImageUrl.split("/");

        String deleteImage = String.join("/", Arrays.copyOfRange(parts, 3, parts.length));

        s3Service.deleteFile(deleteImage);

        String newImageUrl = s3Service.uploadFile(newImageFile, ROOM);
        imageToUpdate.setUrl(newImageUrl);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EasyCheckException(ROOM_NOT_FOUND));

        for (RoomEntity.ImageEntity image : room.getImages()) {
            String oldImageUrl = image.getUrl();

            String[] parts = oldImageUrl.split("/");
            String deleteImage = String.join("/", Arrays.copyOfRange(parts, 3, parts.length));

            s3Service.deleteFile(deleteImage);
        }
        roomRepository.delete(room);
    }

}

package com.beyond.easycheck.rooms.ui.controller;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.rooms.application.dto.RoomFindQuery;
import com.beyond.easycheck.rooms.application.service.RoomService;
import com.beyond.easycheck.rooms.exception.RoomMessageType;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomImageRepository;
import com.beyond.easycheck.rooms.ui.requestbody.RoomCreateRequest;
import com.beyond.easycheck.rooms.ui.requestbody.RoomUpdateRequest;
import com.beyond.easycheck.rooms.ui.view.RoomView;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomtypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.beyond.easycheck.rooms.exception.RoomMessageType.ROOM_IMAGE_NOT_FOUND;
import static com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType.ROOM_TYPE_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@Tag(name = "Room", description = "객실 관리 API")
@RequestMapping("api/v1/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomtypeRepository roomtypeRepository;
    private final RoomImageRepository roomImageRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "객실 생성 API")
    public ResponseEntity<RoomEntity> createRoom(
            @RequestPart("description") @Valid RoomCreateRequest roomCreateRequest,
            @RequestPart("pic") List<MultipartFile> imageFiles) {

        RoomEntity createdRoom = roomService.createRoom(roomCreateRequest, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 단일 조회 API")
    public ResponseEntity<RoomView> readRoom(@PathVariable Long id) {
        RoomView roomView = roomService.readRoom(id);
        return ResponseEntity.ok().body(roomView);
    }

    @GetMapping("")
    @Operation(summary = "객실 전체 조회 API")
    public ResponseEntity<List<RoomView>> readRooms(
            @RequestParam(required = false) Long accommodationId,
            @RequestParam(required = false) Long roomTypeId
    ) {
        RoomFindQuery query = new RoomFindQuery(accommodationId, roomTypeId);

        List<RoomView> roomViews = roomService.readRooms(query);

        return ResponseEntity.ok().body(roomViews);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "객실 수정 API")
    public ResponseEntity<Void> updateRoom(@PathVariable Long id, @RequestBody RoomUpdateRequest roomUpdateRequest) {

        RoomtypeEntity roomtypeEntity = roomtypeRepository.findById(roomUpdateRequest.getRoomtypeEntity())
                .orElseThrow(() -> new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        if (roomUpdateRequest.getRoomNumber() == null || roomUpdateRequest.getRoomNumber().isEmpty()
                || roomUpdateRequest.getRoomAmount() < 0) {
            throw new EasyCheckException(RoomMessageType.ARGUMENT_NOT_VALID);
        }

        roomService.updateRoom(id, roomUpdateRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "객실 사진 수정 API")
    public ResponseEntity<Void> updateRoomImage(@PathVariable Long imageId, @RequestPart MultipartFile newImageFile) {
        RoomEntity.ImageEntity imageEntity = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new EasyCheckException(ROOM_IMAGE_NOT_FOUND));

        roomService.updateRoomImage(imageId, newImageFile);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 삭제 API")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

}

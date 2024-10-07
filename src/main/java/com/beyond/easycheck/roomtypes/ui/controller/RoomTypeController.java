package com.beyond.easycheck.roomtypes.ui.controller;

import com.beyond.easycheck.roomtypes.application.service.RoomTypeService;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomTypeCreateRequest;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomTypeReadRequest;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomTypeUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "RoomType", description = "객실 유형 관리 API")
@RequestMapping("/api/v1/roomType")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping("")
    @Operation(summary = "객실 유형 생성 API")
    public ResponseEntity<Void> createRoomType(@RequestBody RoomTypeCreateRequest roomTypeCreateRequest) {
        roomTypeService.createRoomType(roomTypeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 유형 조회 API")
    public ResponseEntity<RoomTypeReadRequest> readRoomType(@PathVariable Long id) {
        RoomTypeReadRequest roomTypeReadRequest = roomTypeService.readRoomType(id);
        return ResponseEntity.ok().body(roomTypeReadRequest);
    }

    @GetMapping("")
    @Operation(summary = "객실 유형 전체 조회 API")
    public ResponseEntity<List<RoomTypeReadRequest>> readRoomTypes() {
        List<RoomTypeReadRequest> roomTypeReadRequests = roomTypeService.readRoomTypes();
        return ResponseEntity.ok().body(roomTypeReadRequests);
    }

    @PutMapping("/{id}")
    @Operation(summary = "객실 유형 수정 API")
    public ResponseEntity<Void> updateRoomType(@PathVariable Long id, @RequestBody RoomTypeUpdateRequest roomTypeUpdateRequest) {
        roomTypeService.updateRoomType(id, roomTypeUpdateRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 유형 삭제 API")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }
}

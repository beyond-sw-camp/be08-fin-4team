package com.beyond.easycheck.roomtypes.ui.controller;

import com.beyond.easycheck.roomtypes.application.dto.RoomTypeFindQuery;
import com.beyond.easycheck.roomtypes.application.service.RoomtypeService;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeCreateRequest;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeUpdateRequest;
import com.beyond.easycheck.roomtypes.ui.view.RoomtypeView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Roomtype", description = "객실 유형 관리 API")
@RequestMapping("/api/v1/roomtypes")
public class RoomtypeController {

    private final RoomtypeService roomTypeService;

    @PostMapping("")
    @Operation(summary = "객실 유형 생성 API")
    public ResponseEntity<Void> createRoomtype(@RequestBody RoomtypeCreateRequest roomTypeCreateRequest) {
        roomTypeService.createRoomtype(roomTypeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 유형 조회 API")
    public ResponseEntity<RoomtypeView> readRoomtype(@PathVariable Long id) {
        RoomtypeView roomtypeView = roomTypeService.readRoomtype(id);
        return ResponseEntity.ok().body(roomtypeView);
    }

    @GetMapping("")
    @Operation(summary = "객실 유형 전체 조회 API")
    public ResponseEntity<List<RoomtypeView>> readRoomtypes(
            @RequestParam(required = false) Long accommodationId
    ) {
        RoomTypeFindQuery query = new RoomTypeFindQuery(accommodationId);

        List<RoomtypeView> roomtypeViews = roomTypeService.readRoomtypes(query);

        return ResponseEntity.ok().body(roomtypeViews);
    }

    @PutMapping("/{id}")
    @Operation(summary = "객실 유형 수정 API")
    public ResponseEntity<Void> updateRoomtype(@PathVariable Long id, @RequestBody RoomtypeUpdateRequest roomTypeUpdateRequest) {
        roomTypeService.updateRoomtype(id, roomTypeUpdateRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 유형 삭제 API")
    public ResponseEntity<Void> deleteRoomtype(@PathVariable Long id) {
        roomTypeService.deleteRoomtype(id);
        return ResponseEntity.noContent().build();
    }
}


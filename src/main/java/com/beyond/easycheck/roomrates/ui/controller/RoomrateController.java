package com.beyond.easycheck.roomrates.ui.controller;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomrates.application.dto.RoomRateFindQuery;
import com.beyond.easycheck.roomrates.application.service.RoomRateService;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateCreateRequest;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateUpdateRequest;
import com.beyond.easycheck.roomrates.ui.view.RoomRateView;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.beyond.easycheck.roomrates.exception.RoomrateMessageType.ARGUMENT_NOT_VALID;
import static com.beyond.easycheck.rooms.exception.RoomMessageType.ROOM_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@Tag(name = "RoomRate", description = "객실 요금 관리 API")
@RequestMapping("api/v1/roomrates")
public class RoomrateController {

    private final RoomRateService roomrateService;
    private final RoomRepository roomRepository;

    @PostMapping("")
    @Operation(summary = "객실 요금 생성 API")
    public ResponseEntity<Void> createRoomrate(@RequestBody RoomrateCreateRequest roomrateCreateRequest) {
        if (roomrateCreateRequest.getRate() == null || roomrateCreateRequest.getRateType() == null) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }

        roomrateService.createRoomRate(roomrateCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 요금 단일 조회 API")
    public ResponseEntity<RoomRateView> readRoomrate(@PathVariable Long id) {
        RoomRateView roomrateView = roomrateService.readRoomRate(id);
        return ResponseEntity.ok().body(roomrateView);
    }

    @GetMapping("")
    @Operation(summary = "객실 요금 전체 조회 API")
    public ResponseEntity<List<RoomRateView>> readRoomrates(
            @RequestParam(required = false) Long roomId, @RequestParam(required = false) LocalDate startDate
            ) {
        RoomRateFindQuery query = new RoomRateFindQuery(roomId, startDate);

        List<RoomRateView> roomRateViews = roomrateService.readRoomRates(query);
        return ResponseEntity.ok().body(roomRateViews);
    }

    @PutMapping("/{id}")
    @Operation(summary = "객실 요금 수정 API")
    public ResponseEntity<Void> updateRoomrate(@PathVariable Long id, @RequestBody RoomrateUpdateRequest roomrateUpdateRequest) {
        if (roomrateUpdateRequest.getRate() == null || roomrateUpdateRequest.getRateType() == null) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }

        if (roomrateUpdateRequest.getRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }

        if (!roomRepository.findById(roomrateUpdateRequest.getRoomEntity()).isPresent()) {
            throw new EasyCheckException(ROOM_NOT_FOUND);
        }

        roomrateService.updateRoomRate(id, roomrateUpdateRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 요금 삭제 API")
    public ResponseEntity<Void> deleteRoomrate(@PathVariable Long id) {
        roomrateService.deleteRoomRate(id);
        return ResponseEntity.noContent().build();
    }
    
}

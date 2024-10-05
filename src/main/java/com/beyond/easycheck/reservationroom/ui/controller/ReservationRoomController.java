package com.beyond.easycheck.reservationroom.ui.controller;

import com.beyond.easycheck.reservationroom.application.service.ReservationRoomService;
import com.beyond.easycheck.reservationroom.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationroom.ui.requestbody.ReservationRoomCreateRequest;
import com.beyond.easycheck.reservationroom.ui.requestbody.ReservationRoomUpdateRequest;
import com.beyond.easycheck.reservationroom.ui.view.ReservationRoomView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ReservationRoom", description = "객실 예약 관리")
@RestController
@RequestMapping("/api/v1/reservation-room")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class ReservationRoomController {

    private final ReservationRoomService reservationRoomService;

    @Operation(summary = "객실을 예약하는 API")
    @PostMapping("")
    public ResponseEntity<ReservationRoomEntity> createReservation(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ReservationRoomCreateRequest reservationRoomCreateRequest) {

        reservationRoomService.createReservation(userId, reservationRoomCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "예약 내역 리스트를 조회하는 API")
    @GetMapping("")
    public ResponseEntity<List<ReservationRoomView>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ReservationRoomView> reservations = reservationRoomService.getAllReservations(page, size);

        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "특정 예약 내역을 조회하는 API")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationRoomView> getReservationById(@PathVariable("id") Long id) {

        ReservationRoomView reservationRoomView = reservationRoomService.getReservationById(id);

        return ResponseEntity.ok(reservationRoomView);
    }

    @Operation(summary = "예약을 취소하는 API")
    @PutMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable("id") Long id,
                                                  @RequestBody @Valid ReservationRoomUpdateRequest reservationRoomUpdateRequest) {

        reservationRoomService.cancelReservation(id, reservationRoomUpdateRequest);

        return ResponseEntity.noContent().build();
    }
}

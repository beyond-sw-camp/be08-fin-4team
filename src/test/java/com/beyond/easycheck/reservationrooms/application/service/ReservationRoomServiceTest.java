package com.beyond.easycheck.reservationrooms.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.mail.application.service.MailService;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.repository.ReservationRoomRepository;
import com.beyond.easycheck.reservationrooms.ui.requestbody.ReservationRoomCreateRequest;
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.rooms.infrastructure.entity.DailyRoomAvailabilityEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.DailyRoomAvailabilityRepository;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ReservationRoomServiceTest {

    @InjectMocks
    private ReservationRoomService reservationRoomService;

    @Mock
    private ReservationRoomRepository reservationRoomRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private DailyRoomAvailabilityRepository dailyRoomAvailabilityRepository;

    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReservation_Success() {

        // given
        Long userId = 1L;
        Long roomId = 1L;

        UserEntity mockUserEntity = mock(UserEntity.class);
        when(mockUserEntity.getId()).thenReturn(userId);
        when(mockUserEntity.getName()).thenReturn("John Doe");
        when(mockUserEntity.getEmail()).thenReturn("johndoe@example.com");

        RoomEntity mockRoomEntity = mock(RoomEntity.class);
        when(mockRoomEntity.getRoomId()).thenReturn(roomId);

        RoomtypeEntity mockRoomTypeEntity = mock(RoomtypeEntity.class);
        when(mockRoomTypeEntity.getName()).thenReturn("Deluxe");

        when(mockRoomEntity.getRoomTypeEntity()).thenReturn(mockRoomTypeEntity);

        ReservationRoomCreateRequest request = new ReservationRoomCreateRequest(
                roomId,
                LocalDateTime.now(),
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 3),
                null,
                10000,
                null
        );

        DailyRoomAvailabilityEntity mockDailyAvailability = mock(DailyRoomAvailabilityEntity.class);
        when(mockDailyAvailability.getRemainingRoom()).thenReturn(5);
        when(mockDailyAvailability.getStatus()).thenReturn(RoomStatus.예약가능);

        given(userJpaRepository.findById(userId)).willReturn(Optional.of(mockUserEntity));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoomEntity));
        given(dailyRoomAvailabilityRepository.findByRoomEntityAndDate(any(RoomEntity.class), any(LocalDateTime.class)))
                .willReturn(Optional.of(mockDailyAvailability));

        doNothing().when(mailService).sendReservationConfirmationEmail(anyString(), any(ReservationRoomView.class));

        // when
        ReservationRoomEntity result = reservationRoomService.createReservation(userId, request);

        // then
        assertNotNull(result);
        assertEquals(result.getUserEntity(), mockUserEntity);
        assertEquals(result.getRoomEntity(), mockRoomEntity);
        assertEquals(result.getTotalPrice(), request.getTotalPrice());
        verify(reservationRoomRepository).save(any(ReservationRoomEntity.class));
        verify(mailService).sendReservationConfirmationEmail(eq("johndoe@example.com"), any(ReservationRoomView.class));
    }

    @Test
    void createReservation_Failure_UserNotFound() {

        // given
        Long userId = 1L;
        ReservationRoomCreateRequest request = new ReservationRoomCreateRequest(
                1L,
                LocalDateTime.now(),
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 3),
                ReservationStatus.RESERVATION,
                10000,
                null
        );

        given(userJpaRepository.findById(userId)).willReturn(Optional.empty());

        // when / then
        EasyCheckException exception = assertThrows(EasyCheckException.class,
                () -> reservationRoomService.createReservation(userId, request));

        assertEquals("User not found in the system", exception.getMessage());
    }

    @Test
    void createReservation_Failure_RoomNotFound() {

        // given
        Long userId = 1L;
        Long roomId = 1L;

        UserEntity mockUserEntity = mock(UserEntity.class);
        when(mockUserEntity.getId()).thenReturn(userId);
        when(mockUserEntity.getName()).thenReturn("John Doe");

        RoomEntity mockRoomEntity = mock(RoomEntity.class);
        when(mockRoomEntity.getRoomId()).thenReturn(roomId);

        ReservationRoomCreateRequest request = new ReservationRoomCreateRequest(
                roomId,
                LocalDateTime.now(),
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 3),
                ReservationStatus.RESERVATION,
                10000,
                null
        );

        given(userJpaRepository.findById(userId)).willReturn(Optional.of(mockUserEntity));
        given(roomRepository.findById(roomId)).willReturn(Optional.empty());

        // when / then
        EasyCheckException exception = assertThrows(EasyCheckException.class,
                () -> reservationRoomService.createReservation(userId, request));

        assertEquals("해당 ID의 룸이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void getReservationById_Success() {

        // given
        Long reservationId = 1L;

        UserEntity mockUserEntity = mock(UserEntity.class);
        when(mockUserEntity.getName()).thenReturn("John Doe");

        RoomtypeEntity mockRoomTypeEntity = mock(RoomtypeEntity.class);
        when(mockRoomTypeEntity.getName()).thenReturn("Deluxe");

        RoomEntity mockRoomEntity = mock(RoomEntity.class);
        when(mockRoomEntity.getRoomId()).thenReturn(1L);
        when(mockRoomEntity.getImages()).thenReturn(List.of());
        when(mockRoomEntity.getRoomTypeEntity()).thenReturn(mockRoomTypeEntity);

        ReservationRoomEntity reservationRoomEntity = mock(ReservationRoomEntity.class);
        when(reservationRoomEntity.getRoomEntity()).thenReturn(mockRoomEntity);
        when(reservationRoomEntity.getUserEntity()).thenReturn(mockUserEntity);

        given(reservationRoomRepository.findById(reservationId)).willReturn(Optional.of(reservationRoomEntity));

        // when
        ReservationRoomView result = reservationRoomService.getReservationById(reservationId);

        // then
        assertNotNull(result);
        verify(reservationRoomRepository).findById(reservationId);
    }

    @Test
    void getReservationById_Failure_ReservationNotFound() {
        // given
        Long reservationId = 1L;

        given(reservationRoomRepository.findById(reservationId)).willReturn(Optional.empty());

        // when / then
        EasyCheckException exception = assertThrows(EasyCheckException.class,
                () -> reservationRoomService.getReservationById(reservationId));

        assertEquals("Reservation not found", exception.getMessage());
    }
}
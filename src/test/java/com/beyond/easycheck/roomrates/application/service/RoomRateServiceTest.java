package com.beyond.easycheck.roomrates.application.service;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomrates.exception.RoomrateMessageType;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomrateType;
import com.beyond.easycheck.roomrates.infrastructure.repository.RoomRateRepository;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateCreateRequest;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateUpdateRequest;
import com.beyond.easycheck.roomrates.ui.view.RoomRateView;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomtypeRepository;
import com.beyond.easycheck.seasons.infrastructure.entity.SeasonEntity;
import com.beyond.easycheck.seasons.infrastructure.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.beyond.easycheck.roomrates.exception.RoomrateMessageType.*;
import static com.beyond.easycheck.rooms.exception.RoomMessageType.ROOM_NOT_FOUND;
import static com.beyond.easycheck.seasons.exception.SeasonMessageType.SEASON_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class RoomRateServiceTest {

    @Mock
    private RoomRateRepository roomrateRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private RoomtypeRepository roomtypeRepository;

    @InjectMocks
    private RoomRateService roomrateService;

    RoomEntity roomEntity;
    SeasonEntity seasonEntity;
    RoomtypeEntity roomtypeEntity;
    AccommodationEntity accommodationEntity;

    @BeforeEach
    void setUp() {
        accommodationEntity = new AccommodationEntity(
                1L,
                "선셋 리조트",
                "123 해변로, 오션 시티",
                AccommodationType.RESORT
        );

        roomtypeEntity = new RoomtypeEntity(
                1L,
                accommodationEntity,
                "디럭스",
                "한 명이 묵을 수 있는 아늑한 룸",
                1
        );

        roomEntity = new RoomEntity(
                1L,
                roomtypeEntity,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        seasonEntity = new SeasonEntity(
                1L,
                "봄",
                "꽃이 만개하는 봄, 야외 활동과 꽃놀이의 계절입니다.",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 5, 31)
        );

        accommodationRepository.save(accommodationEntity);
        roomtypeRepository.save(roomtypeEntity);
        roomRepository.save(roomEntity);
        seasonRepository.save(seasonEntity);
    }

    @Test
    @DisplayName("객실 요금 생성 성공")
    void createRoomRate_success() {
        // Given
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(seasonEntity));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));

        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                1L,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        RoomRateEntity roomrateEntity = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        when(roomrateRepository.save(any(RoomRateEntity.class))).thenReturn(roomrateEntity);

        // When
        roomrateService.createRoomRate(roomrateCreateRequest);

        // When & Then
        assertThatCode(() -> roomrateService.createRoomRate(roomrateCreateRequest))
                .doesNotThrowAnyException();

        verify(roomRepository).save(any(RoomEntity.class));
    }

    @Test
    @DisplayName("객실 요금 생성 실패 - 존재하지 않는 roomId")
    void createRoomRate_fail_wrongRoomId() {
        // Given
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                999L,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        // When & Then
        assertThatThrownBy(() -> roomrateService.createRoomRate(roomrateCreateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_NOT_FOUND.getMessage());

        verify(roomrateRepository, never()).save(any(RoomRateEntity.class));
    }

    @Test
    @DisplayName("객실 요금 생성 실패 - 존재하지 않는 seasonId")
    void createRoomRate_fail_wrongSeasonId() {
        // Given
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                1L,
                999L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomrateService.createRoomRate(roomrateCreateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(SEASON_NOT_FOUND.getMessage());

        verify(roomrateRepository, never()).save(any(RoomRateEntity.class));
    }

    @Test
    @DisplayName("객실 요금 생성 실패 - 잘못된 입력값")
    void createRoomRate_fail_wrongValue() {
        // Given
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                1L,
                1L,
                null,
                null
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(seasonEntity));

        // When & Then
        assertThatThrownBy(() -> roomrateService.createRoomRate(roomrateCreateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(RoomrateMessageType.ARGUMENT_NOT_VALID.getMessage());

        verify(roomrateRepository, never()).save(any(RoomRateEntity.class));
    }

    @Test
    @DisplayName("객실 요금 단일 조회 성공")
    void readRoomRate_success() {
        // Given
        RoomRateEntity roomrateEntity = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        when(roomtypeRepository.findById(1L)).thenReturn(Optional.of(roomtypeEntity));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(seasonEntity));
        when(roomrateRepository.findById(1L)).thenReturn(Optional.of(roomrateEntity));

        RoomRateView roomrateView = new RoomRateView(
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000),
                RoomStatus.예약가능,
                "디럭스",
                "봄"
        );

        // When
        RoomRateView readRoomrate = roomrateService.readRoomRate(1L);

        // Then
        assertThat(readRoomrate.getId()).isEqualTo(roomrateView.getId());
        assertThat(readRoomrate.getRateType()).isEqualTo(roomrateView.getRateType());
        assertThat(readRoomrate.getRate()).isEqualTo(roomrateView.getRate());
        assertThat(readRoomrate.getStatus()).isEqualTo(roomrateView.getStatus());
        assertThat(readRoomrate.getTypeName()).isEqualTo(roomrateView.getTypeName());
        assertThat(readRoomrate.getSeasonName()).isEqualTo(roomrateView.getSeasonName());
    }

    @Test
    @DisplayName("객실 요금 단일 조회 실패 - 존재하지 않는 roomrateId")
    void readRoomRate_fail() {
        // Given
        Long roomrateId = 999L;

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(seasonEntity));

        // When & Then
        assertThatThrownBy(() -> roomrateService.readRoomRate(roomrateId))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(RoomrateMessageType.ROOM_RATE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("객실 요금 전체 조회 성공")
    void readRoomRates_success() {
        // Given
        RoomRateEntity roomrate1 = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        RoomRateEntity roomrate2 = new RoomRateEntity(
                2L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        List<RoomRateEntity> roomrateEntities = Arrays.asList(roomrate1, roomrate2);
        when(roomrateRepository.findAll()).thenReturn(roomrateEntities);

        // When
        List<RoomRateView> roomRateViews = roomrateService.readRoomRates();

        // Then
        assertThat(roomRateViews).hasSize(2);
        assertThat(roomRateViews.get(0).getId()).isEqualTo(roomrate1.getId());
        assertThat(roomRateViews.get(1).getId()).isEqualTo(roomrate2.getId());

        verify(roomrateRepository).findAll();
    }

    @Test
    @DisplayName("객실 요금 전체 조회 실패 - 빈 리스트")
    void readRoomRates_fail() {
        // Given
        when(roomrateRepository.findAll()).thenThrow(new EasyCheckException(ROOM_RATES_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> roomrateService.readRoomRates())
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_RATES_NOT_FOUND.getMessage());

        verify(roomrateRepository).findAll();
    }

    @Test
    @DisplayName("객실 요금 정보 수정 성공")
    void updateRoomRate_success() {
        // Given
        RoomRateEntity roomrate = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        RoomrateUpdateRequest updateRoomrateRequest = new RoomrateUpdateRequest(
                1L,
                1L,
                RoomrateType.평일,
                BigDecimal.valueOf(200000)
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(seasonEntity));
        when(roomrateRepository.findById(1L)).thenReturn(Optional.of(roomrate));

        // When
        roomrateService.updateRoomRate(1L, updateRoomrateRequest);

        // Then
        assertThat(updateRoomrateRequest.getRoomEntity()).isEqualTo(1L);
        assertThat(updateRoomrateRequest.getSeasonEntity()).isEqualTo(1L);
        assertThat(updateRoomrateRequest.getRateType()).isEqualTo(RoomrateType.평일);
        assertThat(updateRoomrateRequest.getRate()).isEqualTo(BigDecimal.valueOf(200000));

        verify(roomrateRepository).findById(1L);
    }

    @Test
    @DisplayName("객실 요금 정보 수정 실패 - 존재하지 않는 roomId")
    void updateRoomRate_fail_wrongRoomId() {
        // Given
        RoomRateEntity roomrate = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        RoomrateUpdateRequest updateRoomrateRequest = new RoomrateUpdateRequest(
                999L,
                1L,
                RoomrateType.평일,
                BigDecimal.valueOf(200000)
        );

        when(roomrateRepository.findById(1L)).thenReturn(Optional.of(roomrate));

        // When & Then
        assertThatThrownBy(() -> roomrateService.updateRoomRate(1L, updateRoomrateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_NOT_FOUND.getMessage());

        verify(roomrateRepository).findById(1L);
        verify(roomrateRepository, never()).save(any(RoomRateEntity.class));
    }

    @Test
    @DisplayName("객실 요금 정보 수정 실패 - 존재하지 않는 seasonId")
    void updateRoomRate_fail_wrongSeasonId() {
        // Given
        RoomRateEntity roomrate = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        RoomrateUpdateRequest updateRoomrateRequest = new RoomrateUpdateRequest(
                1L,
                999L,
                RoomrateType.평일,
                BigDecimal.valueOf(200000)
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(roomrateRepository.findById(1L)).thenReturn(Optional.of(roomrate));

        // When & Then
        assertThatThrownBy(() -> roomrateService.updateRoomRate(1L, updateRoomrateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(SEASON_NOT_FOUND.getMessage());

        verify(roomrateRepository).findById(1L);
        verify(roomrateRepository, never()).save(any(RoomRateEntity.class));
    }

    @Test
    @DisplayName("객실 요금 정보 수정 실패 - 잘못된 입력값")
    void updateRoom_fail() {
        // Given
        RoomRateEntity roomrate = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        RoomrateUpdateRequest updateRoomrateRequest = new RoomrateUpdateRequest(
                1L,
                1L,
                null,
                null
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(seasonEntity));
        when(roomrateRepository.findById(1L)).thenReturn(Optional.of(roomrate));

        // When & Then
        assertThatThrownBy(() -> roomrateService.updateRoomRate(1L, updateRoomrateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ARGUMENT_NOT_VALID.getMessage());

        verify(roomrateRepository).findById(1L);
        verify(roomrateRepository, never()).save(any(RoomRateEntity.class));
    }

    @Test
    @DisplayName("객실 요금 정보 삭제 성공")
    void deleteRoomRate_success() {
        // Given
        Long roomrateId = 1L;

        RoomRateEntity roomrate = new RoomRateEntity(
                1L,
                roomEntity,
                seasonEntity,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        when(roomrateRepository.findById(roomrateId)).thenReturn(Optional.of(roomrate));

        // When
        roomrateService.deleteRoomRate(roomrateId);

        // Then
        verify(roomrateRepository).delete(roomrate);
    }

    @Test
    @DisplayName("객실 요금 정보 삭제 실패 - 잘못된 RoomrateID")
    void deleteRoomRate_fail() {
        Long roomrateId = 999L;

        when(roomrateRepository.findById(roomrateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomrateService.deleteRoomRate(roomrateId))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_RATE_NOT_FOUND.getMessage());

        verify(roomrateRepository).findById(roomrateId);
    }
}

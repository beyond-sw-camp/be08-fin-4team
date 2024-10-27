package com.beyond.easycheck.roomrates.ui.controller;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomrates.application.service.RoomRateService;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomrateType;
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
import com.beyond.easycheck.user.application.mock.WithEasyCheckMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.beyond.easycheck.roomrates.exception.RoomrateMessageType.ARGUMENT_NOT_VALID;
import static com.beyond.easycheck.roomrates.exception.RoomrateMessageType.ROOM_RATE_NOT_FOUND;
import static com.beyond.easycheck.rooms.exception.RoomMessageType.ROOM_NOT_FOUND;
import static com.beyond.easycheck.seasons.exception.SeasonMessageType.SEASON_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RoomrateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RoomRateService roomrateService;

    @MockBean
    private AccommodationRepository accommodationRepository;

    @MockBean
    private RoomtypeRepository roomtypeRepository;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private SeasonRepository seasonRepository;

    AccommodationEntity accommodationEntity;
    RoomEntity roomEntity;
    SeasonEntity seasonEntity;
    RoomtypeEntity roomtypeEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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

        when(accommodationRepository.findById(accommodationEntity.getId())).thenReturn(Optional.of(accommodationEntity));
        when(roomtypeRepository.findById(roomtypeEntity.getRoomTypeId())).thenReturn(Optional.of(roomtypeEntity));
        when(roomRepository.findById(roomEntity.getRoomId())).thenReturn(Optional.of(roomEntity));
        when(seasonRepository.findById(seasonEntity.getId())).thenReturn(Optional.of(seasonEntity));
    }

    @Test
    @DisplayName("객실 요금 생성 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoomrate() throws Exception {
        // Given
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                1L,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        // When
        ResultActions perform = mockMvc.perform(post("/api/v1/roomrates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateCreateRequest))
        );

        // Then
        perform.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("객실 요금 생성 실패 - 존재하지 않는 roomID")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoomrates_fail_wrongRoomId() throws Exception {
        // Given
        Long roomId = 999L;
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                roomId,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        doThrow(new EasyCheckException(ROOM_NOT_FOUND)).when(roomrateService).createRoomRate(any(RoomrateCreateRequest.class));

        // When
        ResultActions perform = mockMvc.perform(post("/api/v1/roomrates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateCreateRequest)));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 요금 생성 실패 - 존재하지 않는 seasonId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoomrates_fail_wrongSeasonId() throws Exception {
        // Given
        Long seasonId = 999L;
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                1L,
                999L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        doThrow(new EasyCheckException(SEASON_NOT_FOUND)).when(roomrateService).createRoomRate(any(RoomrateCreateRequest.class));

        // When
        ResultActions perform = mockMvc.perform(post("/api/v1/roomrates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateCreateRequest)));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(SEASON_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(SEASON_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 요금 생성 실패 - 잘못된 입력값")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoomrates_fail_wrongValue() throws Exception {
        // Given
        RoomrateCreateRequest roomrateCreateRequest = new RoomrateCreateRequest(
                1L,
                1L,
                null,
                null
        );

        doThrow(new EasyCheckException(ARGUMENT_NOT_VALID)).when(roomrateService).createRoomRate(any(RoomrateCreateRequest.class));

        // When
        ResultActions perform = mockMvc.perform(post("/api/v1/roomrates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateCreateRequest)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].errorType").value(ARGUMENT_NOT_VALID.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ARGUMENT_NOT_VALID.getMessage()));
    }

    @Test
    @DisplayName("객실 요금 단일 조회 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRoomrate_success() throws Exception {
        // Given
        Long id = 1L;
        RoomRateView roomrateView = new RoomRateView(
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000),
                RoomStatus.예약가능,
                "디럭스",
                "여름"
        );

        when(roomrateService.readRoomRate(id)).thenReturn(roomrateView);

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/roomrates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("객실 요금 단일 조회 실패 - 존재하지 않는 roomrateId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRoomrate_fail() throws Exception {
        // Given
        Long id = 999L;
        when(roomrateService.readRoomRate(id)).thenThrow(new EasyCheckException(ROOM_RATE_NOT_FOUND));

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/roomrates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_RATE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_RATE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 요금 전체 조회 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRoomrates_success() throws Exception {
        // Given
        RoomRateView roomrate1 = new RoomRateView(
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000),
                RoomStatus.예약가능,
                "디럭스",
                "여름"
        );

        RoomRateView roomrate2 = new RoomRateView(
                2L,
                RoomrateType.평일,
                BigDecimal.valueOf(200000),
                RoomStatus.예약가능,
                "디럭스",
                "봄"
        );

        List<RoomRateView> roomRateViews = Arrays.asList(roomrate1, roomrate2);

        when(roomrateService.readRoomRates()).thenReturn(roomRateViews);

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/roomrates")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("객실 요금 전체 조회 실패 - 빈 객실")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRoomrates_fail() throws Exception {
        // Given
        when(roomrateService.readRoomRates()).thenReturn(Collections.emptyList());

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/roomrates")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("객실 요금 수정 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoomrate_success() throws Exception {
        // Given
        Long id = 1L;
        RoomrateUpdateRequest roomrateUpdateRequest = new RoomrateUpdateRequest(
                1L,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(100000)
        );

        // When
        ResultActions perform = mockMvc.perform(put("/api/v1/roomrates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateUpdateRequest)));

        // Then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("객실 요금 수정 실패 - 잘못된 roomId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoomrate_fail_wrongRoomId() throws Exception {
        // Given
        Long roomId = 999L;
        RoomrateUpdateRequest roomrateUpdateRequest = new RoomrateUpdateRequest(
                999L,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(200000)
        );

        // Then
        ResultActions perform = mockMvc.perform(put("/api/v1/roomrates/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateUpdateRequest)));

        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 요금 수정 실패 - 잘못된 seasonId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoomrate_fail_wrongSeasonId() throws Exception {
        // Given
        Long roomrateId = 1L;
        Long seasonId = 999L;
        RoomrateUpdateRequest roomrateUpdateRequest = new RoomrateUpdateRequest(
                roomrateId,
                seasonId,
                RoomrateType.주말,
                BigDecimal.valueOf(200000)
        );

        doThrow(new EasyCheckException(SEASON_NOT_FOUND))
                .when(roomrateService).updateRoomRate(eq(roomrateId), any(RoomrateUpdateRequest.class));

        // When
        ResultActions perform = mockMvc.perform(put("/api/v1/roomrates/{id}", roomrateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateUpdateRequest)));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(SEASON_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(SEASON_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("객실 요금 수정 실패 - 잘못된 입력값")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoomrate_fail_wrongValue() throws Exception {
        // Given
        Long roomrateId = 1L;

        RoomrateUpdateRequest roomrateUpdateRequest = new RoomrateUpdateRequest(
                roomrateId,
                1L,
                RoomrateType.주말,
                BigDecimal.valueOf(-100000)
        );

        // When
        ResultActions perform = mockMvc.perform(put("/api/v1/roomrates/{id}", roomrateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomrateUpdateRequest)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].errorType").value(ARGUMENT_NOT_VALID.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ARGUMENT_NOT_VALID.getMessage()));
    }

    @Test
    @DisplayName("객실 요금 삭제 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void deleteRoomrate_success() throws Exception {
        // Given
        Long roomRateId = 1L;

        doNothing().when(roomrateService).deleteRoomRate(roomRateId);

        // When
        ResultActions perform = mockMvc.perform(delete("/api/v1/roomrates/{id}", roomRateId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("객실 요금 삭제 실패 - 잘못된 roomrateId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void deleteRoomrate_fail() throws Exception {
        // Given
        Long invalidRoomrateId = 999L;

        doThrow(new EasyCheckException(ROOM_RATE_NOT_FOUND))
                .when(roomrateService).deleteRoomRate(invalidRoomrateId);

        // When
        ResultActions perform = mockMvc.perform(delete("/api/v1/roomrates/{id}", invalidRoomrateId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_RATE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_RATE_NOT_FOUND.getMessage()));
    }
}

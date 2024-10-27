package com.beyond.easycheck.roomtypes.ui.controller;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.roomtypes.application.service.RoomtypeService;
import com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeCreateRequest;
import com.beyond.easycheck.user.application.mock.WithEasyCheckMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomtypeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoomtypeService roomtypeService;

    @Autowired
    AccommodationRepository accommodationRepository;

    @Test
    @DisplayName("[객실 유형 조회] - 성공")
    @WithEasyCheckMockUser(id = 1L, role = "USER")
    void readRoomtype() throws Exception {
        // given
        Long id = 1L;
        Long roomTypeId = 1L; // RoomType ID 추가
        AccommodationEntity accommodation = AccommodationEntity.builder()
                .id(id)
                .name("선셋 리조트")
                .address("123 해변로, 오션 시티")
                .accommodationType(AccommodationType.HOTEL)
                .build();

        accommodationRepository.save(accommodation); // Accommodation 저장

        // RoomType 데이터 추가
        RoomtypeCreateRequest roomtypeRequest = new RoomtypeCreateRequest(roomTypeId, "디럭스", "아늑한 룸", 1);
        roomtypeService.createRoomtype(roomtypeRequest); // RoomType 생성

    // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/roomtypes/{id}", roomTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.roomTypeId").value(roomTypeId))
                .andExpect(jsonPath("$.name").value("디럭스"));


    }

    @Test
    @DisplayName("[객실 유형 조회] - 객실 유형 찾기 실패")
    @WithEasyCheckMockUser(id = 1L, role = "USER")
    void readRoomtype_fail() throws Exception {
        // given
        Long nonExistentRoomTypeId = 999L;

        // when & then - MockMvc를 통한 GET 요청 수행 및 예외 검증
        ResultActions perform = mockMvc.perform(
                get("/api/v1/roomtypes/{id}", nonExistentRoomTypeId)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(RoomtypeMessageType.ROOM_TYPE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(RoomtypeMessageType.ROOM_TYPE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("[객실 유형 목록 조회] - 성공")
    @WithEasyCheckMockUser(id = 1L, role = "USER")
    void readRoomtypes() throws Exception {
        // given
        RoomtypeCreateRequest request1 = new RoomtypeCreateRequest(6L,"디럭스","한 명이 묵을 수 있는 아늑한 룸", 1);
        RoomtypeCreateRequest request2 = new RoomtypeCreateRequest(7L, "디럭스 - 원룸", "두 명이 묵을 수 있는 넓은 룸", 2);

        roomtypeService.createRoomtype(request1);
        roomtypeService.createRoomtype(request2);

        // when & then - MockMvc를 통한 GET 요청 수행 및 예외 검증
        ResultActions perform = mockMvc.perform(
                get("/api/v1/roomtypes")
                        .contentType(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].typeName").value(request1.getName()))
                .andExpect(jsonPath("$[0].description").value(request1.getDescription()))
                .andExpect(jsonPath("$[0].maxOccupancy").value(request1.getMaxOccupancy()))
                .andExpect(jsonPath("$[1].typeName").value(request2.getName()))
                .andExpect(jsonPath("$[1].description").value(request2.getDescription()))
                .andExpect(jsonPath("$[1].maxOccupancy").value(request2.getMaxOccupancy()));

    }

    @Test
    @DisplayName("[객실 유형 목록 조회] - 빈 리스트")
    @WithEasyCheckMockUser(id = 1L, role = "USER")
    void readRoomtypes_emptyList() throws Exception{

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/roomtypes")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0)); // 리스트의 크기가 0인지 확인

    }
}
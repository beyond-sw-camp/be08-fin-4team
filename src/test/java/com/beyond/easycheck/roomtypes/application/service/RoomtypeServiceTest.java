package com.beyond.easycheck.roomtypes.application.service;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeCreateRequest;
import com.beyond.easycheck.roomtypes.ui.view.RoomtypeView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@Transactional
class RoomtypeServiceTest {

    @Autowired
    private RoomtypeService roomtypeService;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Test
    @DisplayName("[객실 유형 조회] - 성공")
    void readRoomtype() {
        // given
        Long accommodationId = 1L;
        Long roomTypeId = 1L; // RoomType ID 추가
        AccommodationEntity accommodation = AccommodationEntity.builder()
                .id(accommodationId) // AccommodationEntity의 id를 수동으로 설정
                .name("호텔")
                .address("서울시")
                .accommodationType(AccommodationType.HOTEL)
                .build();

        accommodationRepository.save(accommodation); // Accommodation 저장

        // RoomType 데이터 추가
        RoomtypeCreateRequest roomtypeRequest = new RoomtypeCreateRequest(roomTypeId, "디럭스", "아늑한 룸", 1);
        roomtypeService.createRoomtype(roomtypeRequest); // RoomType 생성

        // when
        RoomtypeView result = roomtypeService.readRoomtype(roomTypeId); // RoomType ID로 조회

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getRoomTypeId()).isEqualTo(roomTypeId); // RoomType ID 검증
        Assertions.assertThat(result.getName()).isEqualTo("디럭스"); // RoomType 이름 검증
    }

    @Test
    @DisplayName("[객실 유형 조회] - 객실 유형 찾기 실패")
    void readRoomtype_fail(){
        // given
        Long nonExistentRoomTypeId = 999L;

        // when & then
        Assertions.assertThatThrownBy(
                () -> {
                    roomtypeService.readRoomtype(nonExistentRoomTypeId);
                })
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(RoomtypeMessageType.ROOM_TYPE_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("[객실 유형 목록 조회] - 성공")
    void readRoomtypes() {
        // given
        RoomtypeCreateRequest request1 = new RoomtypeCreateRequest(6L,"디럭스","한 명이 묵을 수 있는 아늑한 룸", 1);
        RoomtypeCreateRequest request2 = new RoomtypeCreateRequest(7L, "디럭스 - 원룸", "두 명이 묵을 수 있는 넓은 룸", 2);

        roomtypeService.createRoomtype(request1);
        roomtypeService.createRoomtype(request2);

        // when
        List<RoomtypeView> result = roomtypeService.readRoomtypes();

        // then
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.get(0).getName()).isEqualTo(request1.getTypeName());
        Assertions.assertThat(result.get(1).getName()).isEqualTo(request2.getTypeName());

    }

    @Test
    @DisplayName("[객실 유형 목록 조회] - 빈 리스트")
    @Transactional(readOnly = true)
    void readRoomtypes_emptyList(){

        // given
        // when
        List<RoomtypeView> roomtypeViews = roomtypeService.readRoomtypes();
        // then
        assertThat(roomtypeViews.size()).isZero();
    }


}
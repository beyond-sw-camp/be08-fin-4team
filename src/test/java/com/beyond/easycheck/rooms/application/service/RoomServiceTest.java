package com.beyond.easycheck.rooms.application.service;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomImageRepository;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.rooms.ui.requestbody.RoomCreateRequest;
import com.beyond.easycheck.rooms.ui.requestbody.RoomUpdateRequest;
import com.beyond.easycheck.rooms.ui.view.RoomView;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.beyond.easycheck.roomtypes.infrastructure.repository.RoomtypeRepository;
import com.beyond.easycheck.s3.application.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.beyond.easycheck.rooms.exception.RoomMessageType.*;
import static com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType.ROOM_TYPE_NOT_FOUND;
import static com.beyond.easycheck.s3.application.domain.FileManagementCategory.ROOM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RoomServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomtypeRepository roomtypeRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private RoomImageRepository roomImageRepository;

    @InjectMocks
    private RoomService roomService;

    private RoomtypeEntity roomtype1;

    private RoomtypeEntity roomtype2;

    private AccommodationEntity accommodationEntity;

    @BeforeEach
    void setUp() {
        accommodationEntity = new AccommodationEntity(
                1L,
                "선셋 리조트",
                "123 해변로, 오션 시티",
                AccommodationType.RESORT
        );

        roomtype1 = new RoomtypeEntity(
                1L,
                accommodationEntity,
                "디럭스",
                "한 명이 묵을 수 있는 아늑한 룸",
                1
        );

        roomtype2 = new RoomtypeEntity(
                2L,
                accommodationEntity,
                "스탠다드",
                "두 명이 묵을 수 있는 룸",
                2
        );

        when(accommodationRepository.findById(accommodationEntity.getId())).thenReturn(Optional.of(accommodationEntity));
        when(roomtypeRepository.findById(roomtype1.getRoomTypeId())).thenReturn(Optional.of(roomtype1));
        when(roomtypeRepository.findById(roomtype2.getRoomTypeId())).thenReturn(Optional.of(roomtype2));
    }

    @Test
    @DisplayName("객실 생성 성공")
    void createRoom_success() {
        // Given
        when(roomtypeRepository.findById(1L)).thenReturn(Optional.of(roomtype1));

        RoomCreateRequest roomCreateRequest = new RoomCreateRequest(
                1L,
                "402",
                RoomStatus.예약가능,
                10,
                5
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(mock(MultipartFile.class));
        imageFiles.add(mock(MultipartFile.class));

        List<String> imageUrls = List.of("url1", "url2");
        when(s3Service.uploadFiles(imageFiles, ROOM)).thenReturn(imageUrls);

        RoomEntity roomEntity = RoomEntity.builder()
                .roomNumber("402")
                .roomTypeEntity(roomtype1)
                .status(RoomStatus.예약가능)
                .roomAmount(10)
                .remainingRoom(5)
                .build();

        when(roomRepository.save(any(RoomEntity.class))).thenReturn(roomEntity);

        // When
        RoomEntity createdRoom = roomService.createRoom(roomCreateRequest, imageFiles);

        // Then
        assertThat(createdRoom).isNotNull();
        assertThat(createdRoom.getImages()).hasSize(2);
        assertThat(createdRoom.getImages()).extracting("url").containsExactlyInAnyOrderElementsOf(imageUrls);

        verify(roomtypeRepository).findById(1L);
        verify(s3Service).uploadFiles(imageFiles, ROOM);
        verify(roomRepository, times(1)).save(any(RoomEntity.class));
    }

    @Test
    @DisplayName("객실 생성 실패 - 존재하지 않는 roomtypeID")
    void createRoom_fail_wrongRoomtypeId() {
        // Given
        RoomCreateRequest roomCreateRequest = new RoomCreateRequest(
                999L,
                "402",
                RoomStatus.예약가능,
                10,
                5
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(mock(MultipartFile.class));
        imageFiles.add(mock(MultipartFile.class));

        // When & Then
        assertThatThrownBy(() -> roomService.createRoom(roomCreateRequest, imageFiles))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_TYPE_NOT_FOUND.getMessage());

        verify(roomtypeRepository).findById(999L);
        verify(s3Service, never()).uploadFiles(anyList(), any());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    @DisplayName("객실 생성 실패 - 잘못된 입력값")
    void createRoom_fail_wrongValue() {
        // Given
        RoomCreateRequest roomCreateRequest = new RoomCreateRequest(
                1L,
                null,
                null,
                -5,
                -5
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(mock(MultipartFile.class));
        imageFiles.add(mock(MultipartFile.class));

        // When & Then
        assertThatThrownBy(() -> roomService.createRoom(roomCreateRequest, imageFiles))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ARGUMENT_NOT_VALID.getMessage());

        verify(roomtypeRepository).findById(1L);
        verify(s3Service, never()).uploadFiles(anyList(), any());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    @DisplayName("객실 단일 조회 성공")
    void readRoom_success() {
        // Given
        RoomEntity roomEntity = new RoomEntity(
                1L,
                roomtype1,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        RoomEntity.ImageEntity image1 = new RoomEntity.ImageEntity(1L, "url1", roomEntity);
        RoomEntity.ImageEntity image2 = new RoomEntity.ImageEntity(2L, "url2", roomEntity);
        roomEntity.addImage(image1);
        roomEntity.addImage(image2);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));

        // When
        RoomView readRoom = roomService.readRoom(1L);

        // Then
        assertThat(readRoom.getImages()).hasSize(2);
        assertThat(readRoom.getImages()).containsExactlyInAnyOrder("url1", "url2");
    }

    @Test
    @DisplayName("객실 단일 조회 실패 - 존재하지 않는 roomID")
    void readRoom_fail() {
        // Given
        Long roomId = 999L;

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodationEntity));
        when(roomtypeRepository.findById(1L)).thenReturn(Optional.of(roomtype1));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.readRoom(roomId))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_NOT_FOUND.getMessage());

        verify(roomRepository).findById(roomId);
    }

    @Test
    @DisplayName("객실 전체 조회 성공")
    void readRooms_success() {
        // Given
        RoomEntity room1 = new RoomEntity(
                1L,
                roomtype1,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        RoomEntity room2 = new RoomEntity(
                2L,
                roomtype2,
                "403",
                new ArrayList<>(),
                RoomStatus.예약가능,
                8,
                3
        );

        List<RoomEntity> roomEntities = Arrays.asList(room1, room2);
        when(roomRepository.findAll()).thenReturn(roomEntities);

        // When
        List<RoomView> roomViews = roomService.readRooms();

        // Then
        assertThat(roomViews).hasSize(2);
        assertThat(roomViews.get(0).getRoomId()).isEqualTo(room1.getRoomId());
        assertThat(roomViews.get(1).getRoomId()).isEqualTo(room2.getRoomId());

        verify(roomRepository).findAll();
    }

    @Test
    @DisplayName("객실 전체 조회 실패 - 빈 객실")
    void readRooms_fail() {
        // Given
        when(roomRepository.findAll()).thenThrow(new EasyCheckException(ROOMS_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> roomService.readRooms())
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOMS_NOT_FOUND.getMessage());

        verify(roomRepository).findAll();
    }

    @Test
    @DisplayName("객실 정보 수정 성공")
    void updateRoom_success() {
        // Given
        RoomEntity existingRoom = new RoomEntity(
                1L,
                roomtype1,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        RoomUpdateRequest updateRoom = new RoomUpdateRequest(
                roomtype1.getRoomTypeId(),
                "403",
                5,
                RoomStatus.예약불가
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        // When
        roomService.updateRoom(1L, updateRoom);

        // Then
        assertThat(existingRoom.getType()).isEqualTo("403");
        assertThat(existingRoom.getStatus()).isEqualTo(RoomStatus.예약불가);
        assertThat(existingRoom.getRoomAmount()).isEqualTo(5);

        verify(roomRepository).findById(1L);
    }

    @Test
    @DisplayName("객실 정보 수정 실패 - 존재하지 않는 roomtypeId")
    void updateRoom_fail_wrongRoomtypeId() {
        // Given
        Long roomtypeId = 999L;
        RoomEntity existingRoom = new RoomEntity(
                1L,
                roomtype1,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        RoomUpdateRequest updateRequest = new RoomUpdateRequest(
                roomtypeId,
                "403",
                -5,
                RoomStatus.예약불가
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        // When & Then
        assertThatThrownBy(() -> roomService.updateRoom(1L, updateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_TYPE_NOT_FOUND.getMessage());

        verify(roomRepository).findById(1L);
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    @DisplayName("객실 정보 수정 실패 - 잘못된 입력값")
    void updateRoom_fail() {
        // Given
        RoomEntity existingRoom = new RoomEntity(
                1L,
                roomtype1,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        RoomUpdateRequest updateRequest = new RoomUpdateRequest(
                roomtype1.getRoomTypeId(),
                "403",
                -5,
                RoomStatus.예약불가
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        // When & Then
        assertThatThrownBy(() -> roomService.updateRoom(1L, updateRequest))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ARGUMENT_NOT_VALID.getMessage());

        verify(roomRepository).findById(1L);
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    @DisplayName("객실 사진 수정 성공")
    void updateRoomImage_success() {
        // Given
        Long imageId = 1L;
        MultipartFile newImageFile = mock(MultipartFile.class);
        String oldImageUrl = "s3://bucket/old/image.jpg";
        String newImageUrl = "s3://bucket/new/image.jpg";

        RoomEntity.ImageEntity imageEntity = new RoomEntity.ImageEntity(imageId, oldImageUrl, null);

        when(roomImageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));
        when(s3Service.uploadFile(newImageFile, ROOM)).thenReturn(newImageUrl);

        // When
        roomService.updateRoomImage(imageId, newImageFile);

        // Then
        assertThat(imageEntity.getUrl()).isEqualTo(newImageUrl);
        verify(s3Service).deleteFile("old/image.jpg");
        verify(roomImageRepository).findById(imageId);
    }

    @Test
    @DisplayName("객실 사진 수정 실패 - 존재하지 않는 이미지 ID")
    void updateRoomImage_fail() {
        // Given
        Long imageId = 999L;
        MultipartFile newImageFile = mock(MultipartFile.class);

        when(roomImageRepository.findById(imageId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.updateRoomImage(imageId, newImageFile))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_IMAGE_NOT_FOUND.getMessage());

        verify(roomImageRepository).findById(imageId);
        verify(s3Service, never()).deleteFile(anyString());
        verify(s3Service, never()).uploadFile(any(MultipartFile.class), eq(ROOM));
    }

    @Test
    @DisplayName("객실 정보 삭제 성공")
    void deleteRoom_success() {
        // Given
        Long roomId = 1L;
        String oldImageUrl1 = "s3://bucket/old/image1.jpg";
        String oldImageUrl2 = "s3://bucket/old/image2.jpg";

        RoomEntity.ImageEntity image1 = new RoomEntity.ImageEntity(1L, oldImageUrl1, null);
        RoomEntity.ImageEntity image2 = new RoomEntity.ImageEntity(2L, oldImageUrl2, null);

        List<RoomEntity.ImageEntity> images = Arrays.asList(image1, image2);
        RoomEntity roomEntity = new RoomEntity(
                roomId,
                roomtype1,
                "402",
                images,
                RoomStatus.예약가능,
                10,
                5
        );

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));

        // When
        roomService.deleteRoom(roomId);

        // Then
        verify(s3Service).deleteFile("old/image1.jpg");
        verify(s3Service).deleteFile("old/image2.jpg");
        verify(roomRepository).delete(roomEntity);
    }

    @Test
    @DisplayName("객실 정보 삭제 실패 - 잘못된 RoomID")
    void deleteRoom_fail() {
        // Given
        Long roomId = 999L;

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.deleteRoom(roomId))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(ROOM_NOT_FOUND.getMessage());

        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).delete(any(RoomEntity.class));
    }
}

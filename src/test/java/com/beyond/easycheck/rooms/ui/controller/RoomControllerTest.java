package com.beyond.easycheck.rooms.ui.controller;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.rooms.application.service.RoomService;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.beyond.easycheck.rooms.exception.RoomMessageType.*;
import static com.beyond.easycheck.roomtypes.exception.RoomtypeMessageType.ROOM_TYPE_NOT_FOUND;
import static com.beyond.easycheck.s3.application.domain.FileManagementCategory.ROOM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RoomControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    S3Service s3Service;

    @MockBean
    RoomService roomService;

    @MockBean
    private RoomtypeRepository roomtypeRepository;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private AccommodationRepository accommodationRepository;

    @MockBean
    RoomImageRepository roomImageRepository;

    AccommodationEntity accommodationEntity;
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

        RoomEntity.ImageEntity existingImage = new RoomEntity.ImageEntity();
        existingImage.setId(1L);
        existingImage.setUrl("existing-image-url.jpg");

        when(accommodationRepository.findById(accommodationEntity.getId())).thenReturn(Optional.of(accommodationEntity));
        when(roomtypeRepository.findById(roomtypeEntity.getRoomTypeId())).thenReturn(Optional.of(roomtypeEntity));
        when(roomImageRepository.findById(1L)).thenReturn(Optional.of(existingImage));
    }

    @Test
    @DisplayName("객실 생성 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoom() throws Exception {
        // Given
        RoomCreateRequest roomCreateRequest = new RoomCreateRequest(
                1L,
                "402",
                RoomStatus.예약가능,
                10,
                5
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(new MockMultipartFile("pic", "roomPic1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3}));
        imageFiles.add(new MockMultipartFile("pic", "roomPic2.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{4, 5, 6}));

        List<String> imageUrls = List.of("url1", "url2");
        when(s3Service.uploadFiles(anyList(), eq(ROOM))).thenReturn(imageUrls);

        RoomEntity createdRoom = new RoomEntity(
                1L,
                roomtypeEntity,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        when(roomService.createRoom(any(RoomCreateRequest.class), anyList())).thenReturn(createdRoom);

        // When
        ResultActions perform = mockMvc.perform(
                multipart("/api/v1/rooms")
                        .file("pic", imageFiles.get(0).getBytes())
                        .file("pic", imageFiles.get(1).getBytes())
                        .file("description", objectMapper.writeValueAsBytes(roomCreateRequest))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // Then
        perform.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.roomNumber").value("402"))
                .andExpect(jsonPath("$.roomAmount").value(10))
                .andExpect(jsonPath("$.status").value("예약가능"));
    }

    @Test
    @DisplayName("객실 생성 실패 - 존재하지 않는 roomtypeID")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoom_fail() throws Exception {
        // Given
        Long roomtypeId = 999L;

        RoomCreateRequest roomCreateRequest = new RoomCreateRequest(
                roomtypeId,
                "402",
                RoomStatus.예약가능,
                10,
                5
        );

        when(roomService.createRoom(any(RoomCreateRequest.class), anyList()))
                .thenThrow(new EasyCheckException(ROOM_TYPE_NOT_FOUND));

        // When
        ResultActions perform = mockMvc.perform(
                multipart("/api/v1/rooms")
                        .file("pic", new byte[]{1, 2, 3})
                        .file("pic", new byte[]{4, 5, 6})
                        .file("description", objectMapper.writeValueAsBytes(roomCreateRequest))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_TYPE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_TYPE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 생성 실패 - 잘못된 입력값")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void createRoom_fail_wrongValue() throws Exception {
        // Given
        Long roomtypeId = 1L;

        RoomCreateRequest roomCreateRequest = new RoomCreateRequest(
                roomtypeId,
                null,
                null,
                -5,
                -5
        );

        when(roomService.createRoom(any(RoomCreateRequest.class), anyList())).thenThrow(new EasyCheckException(ARGUMENT_NOT_VALID));

        // When
        ResultActions perform = mockMvc.perform(
                multipart("/api/v1/rooms")
                        .file("pic", new byte[]{1, 2, 3})
                        .file("pic", new byte[]{4, 5, 6})
                        .file("description", objectMapper.writeValueAsBytes(roomCreateRequest))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0].errorType").value(ARGUMENT_NOT_VALID.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ARGUMENT_NOT_VALID.getMessage()));
    }

    @Test
    @DisplayName("객실 단일 조회 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRoom_success() throws Exception {
        // Given
        Long id = 1L;

        RoomEntity roomEntity = new RoomEntity(
                id,
                roomtypeEntity,
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

        RoomView roomView = new RoomView (
                id,
                "402",
                List.of("url1", "url2"),
                10,
                5,
                RoomStatus.예약가능,
                roomtypeEntity.getRoomTypeId(),
                roomtypeEntity.getAccommodationEntity().getId(),
                roomtypeEntity.getName(),
                roomtypeEntity.getDescription(),
                4
        );

        when(roomService.readRoom(id)).thenReturn(roomView);

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/rooms/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roomId").value(id));
    }

    @Test
    @DisplayName("객실 단일 조회 실패 - 존재하지 않는 roomId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRoom_fail() throws Exception {
        // Given
        Long id = 999L;
        when(roomService.readRoom(id)).thenThrow(new EasyCheckException(ROOM_NOT_FOUND));

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/rooms/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 전체 조회 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRooms_success() throws Exception {
        // Given
        Long accommodationId = 1L;

        RoomView roomView1 = new RoomView(
                1L,
                "402",
                new ArrayList<>(),
                10,
                5,
                RoomStatus.예약가능,
                1L,
                accommodationId,
                "디럭스",
                "한 명이 살기 좋은 방",
                1
        );

        RoomView roomView2 = new RoomView(
                2L,
                "403",
                new ArrayList<>(),
                8,
                3,
                RoomStatus.예약가능,
                2L,
                accommodationId,
                "스탠다드",
                "두 명이 살기 좋은 방",
                2
        );

        List<RoomView> roomViews = Arrays.asList(roomView1, roomView2);

        when(roomService.readRooms()).thenReturn(roomViews);

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("객실 전체 조회 실패 - 빈 객실")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void readRooms_fail() throws Exception {
        // Given
        when(roomService.readRooms()).thenReturn(Collections.emptyList());

        // When
        ResultActions perform = mockMvc.perform(get("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("객실 수정 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoom_success() throws Exception {
        // Given
        Long roomId = 1L;
        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest(
                1L,
                "501",
                8,
                RoomStatus.예약불가
        );

        // When
        ResultActions perform = mockMvc.perform(patch("/api/v1/rooms/{id}", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomUpdateRequest)));

        // Then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("객실 수정 실패 - 존재하지 않은 roomtypeId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoom_fail_wrongRoomtypeId() throws Exception {
        // Given
        Long roomtypeId = 999L;
        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest(
                roomtypeId,
                "402",
                -5,
                RoomStatus.예약불가
        );

        // When
        ResultActions perform = mockMvc.perform(patch("/api/v1/rooms/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomUpdateRequest)));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_TYPE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_TYPE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 수정 실패 - 잘못된 입력값")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoom_fail() throws Exception {
        // Given
        Long roomId = 1L;
        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest(
                1L,
                "402",
                -5,
                RoomStatus.예약불가
        );

        // When
        ResultActions perform = mockMvc.perform(patch("/api/v1/rooms/{id}", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomUpdateRequest)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].errorType").value(ARGUMENT_NOT_VALID.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ARGUMENT_NOT_VALID.getMessage()));
    }

    @Test
    @DisplayName("객실 사진 수정 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoomImage_success() throws Exception {
        // Given
        Long imageId = 1L;
        MockMultipartFile newImageFile = new MockMultipartFile("newImageFile", "newImage.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        doNothing().when(roomService).updateRoomImage(eq(imageId), any(MultipartFile.class));

        // When
        ResultActions perform = mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/rooms/images/{imageId}", imageId)
                .file(newImageFile)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("객실 사진 삭제 실패 - 존재하지 않는 imageId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void updateRoomImage_fail_wrongImageId() throws Exception {
        // Given
        Long imageId = 1L;
        MockMultipartFile newImageFile = new MockMultipartFile("newImageFile", "newImage.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        doThrow(new EasyCheckException(ROOM_IMAGE_NOT_FOUND)).when(roomService).updateRoomImage(imageId, newImageFile);

        // When
        ResultActions perform = mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/rooms/images/{imageId}", imageId)
                .file(newImageFile)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_IMAGE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_IMAGE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("객실 삭제 성공")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void deleteRoom_success() throws Exception {
        // Given
        Long roomId = 1L;

        RoomEntity room = new RoomEntity(
                roomId,
                roomtypeEntity,
                "402",
                new ArrayList<>(),
                RoomStatus.예약가능,
                10,
                5
        );

        RoomEntity.ImageEntity image1 = new RoomEntity.ImageEntity();
        image1.setUrl("https://example.com/images/room/image1.jpg");
        RoomEntity.ImageEntity image2 = new RoomEntity.ImageEntity();
        image2.setUrl("https://example.com/images/room/image2.jpg");
        room.setImages(Arrays.asList(image1, image2));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        doNothing().when(s3Service).deleteFile(any(String.class));
        doNothing().when(roomRepository).delete(any(RoomEntity.class));

        // When
        ResultActions perform = mockMvc.perform(delete("/api/v1/rooms/{id}", roomId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("객실 삭제 실패 - 존재하지 않는 roomId")
    @WithEasyCheckMockUser(role = "SUPER_ADMIN")
    void deleteRoom_fail() throws Exception {
        // Given
        Long roomId = 999L;

        doThrow(new EasyCheckException(ROOM_NOT_FOUND))
                .when(roomService).deleteRoom(roomId);

        // When
        ResultActions perform = mockMvc.perform(delete("/api/v1/rooms/{id}", roomId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].errorType").value(ROOM_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ROOM_NOT_FOUND.getMessage()));
    }
}

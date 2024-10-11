package com.beyond.easycheck.themeparks.ui.controller;

import com.beyond.easycheck.themeparks.application.service.ThemeParkOperationUseCase;
import com.beyond.easycheck.themeparks.application.service.ThemeParkReadUseCase;
import com.beyond.easycheck.themeparks.application.service.ThemeParkReadUseCase.FindThemeParkResult;
import com.beyond.easycheck.themeparks.ui.requestbody.ThemeParkCreateRequest;
import com.beyond.easycheck.themeparks.ui.requestbody.ThemeParkUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ThemeParkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThemeParkOperationUseCase themeParkOperationUseCase;

    @MockBean
    private ThemeParkReadUseCase themeParkReadUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    // POST 테스트: 테마파크 생성
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateThemeParkSuccessfully() throws Exception {
        // Given
        ThemeParkCreateRequest createRequest = new ThemeParkCreateRequest(
                "테마파크 1",
                "재미있는 테마파크",
                "서울",
                "이미지_주소"
        );

        FindThemeParkResult result = FindThemeParkResult.builder()
                .id(1L)
                .name("테마파크 1")
                .description("재미있는 테마파크")
                .location("서울")
                .image("이미지_주소")
                .build();

        Mockito.when(themeParkOperationUseCase.saveThemePark(any(), eq(1L)))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/accommodations/1/parks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("테마파크 1"))
                .andExpect(jsonPath("$.data.description").value("재미있는 테마파크"))
                .andExpect(jsonPath("$.data.location").value("서울"))
                .andExpect(jsonPath("$.data.image").value("이미지_주소"))
                .andDo(print());
    }

    // GET 테스트: 모든 테마파크 조회
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetAllThemeParksSuccessfully() throws Exception {
        // Given
        List<FindThemeParkResult> results = Collections.singletonList(
                FindThemeParkResult.builder()
                        .id(1L)
                        .name("테마파크 1")
                        .description("재미있는 테마파크")
                        .location("서울")
                        .image("이미지_주소")
                        .build()
        );

        Mockito.when(themeParkReadUseCase.getThemeParks(eq(1L)))
                .thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/accommodations/1/parks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("테마파크 1"))
                .andExpect(jsonPath("$.data[0].description").value("재미있는 테마파크"))
                .andExpect(jsonPath("$.data[0].location").value("서울"))
                .andExpect(jsonPath("$.data[0].image").value("이미지_주소"))
                .andDo(print());
    }

    // GET 테스트: 특정 테마파크 조회
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetThemeParkByIdSuccessfully() throws Exception {
        // Given
        FindThemeParkResult result = FindThemeParkResult.builder()
                .id(1L)
                .name("테마파크 1")
                .description("재미있는 테마파크")
                .location("서울")
                .image("이미지_주소")
                .build();

        Mockito.when(themeParkReadUseCase.getFindThemePark(eq(1L), eq(1L)))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/accommodations/1/parks/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테마파크 1"))
                .andExpect(jsonPath("$.data.description").value("재미있는 테마파크"))
                .andExpect(jsonPath("$.data.location").value("서울"))
                .andExpect(jsonPath("$.data.image").value("이미지_주소"))
                .andDo(print());
    }

    // PUT 테스트: 테마파크 수정
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateThemeParkSuccessfully() throws Exception {
        // Given
        ThemeParkUpdateRequest updateRequest = new ThemeParkUpdateRequest(
                "새로운 테마파크 이름",
                "업데이트된 설명",
                "부산",
                "새로운 이미지"
        );

        FindThemeParkResult result = FindThemeParkResult.builder()
                .id(1L)
                .name("새로운 테마파크 이름")
                .description("업데이트된 설명")
                .location("부산")
                .image("새로운 이미지")
                .build();

        Mockito.when(themeParkOperationUseCase.updateThemePark(eq(1L), any(), eq(1L)))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(put("/api/v1/accommodations/1/parks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("새로운 테마파크 이름"))
                .andExpect(jsonPath("$.data.description").value("업데이트된 설명"))
                .andExpect(jsonPath("$.data.location").value("부산"))
                .andExpect(jsonPath("$.data.image").value("새로운 이미지"))
                .andDo(print());
    }

    // DELETE 테스트: 테마파크 삭제
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteThemeParkSuccessfully() throws Exception {
        // Given
        Mockito.doNothing().when(themeParkOperationUseCase).deleteThemePark(eq(1L), eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/v1/accommodations/1/parks/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());

        Mockito.verify(themeParkOperationUseCase, Mockito.times(1)).deleteThemePark(eq(1L), eq(1L));
    }

}


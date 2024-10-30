//package com.beyond.easycheck.payments.ui.controller;
//
//import com.beyond.easycheck.payments.application.service.PaymentService;
//import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
//import com.beyond.easycheck.payments.ui.requestbody.PaymentCreateRequest;
//import com.beyond.easycheck.payments.ui.requestbody.PaymentUpdateRequest;
//import com.beyond.easycheck.payments.ui.view.PaymentView;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//public class PaymentControllerTest {
//
//    @MockBean
//    private PaymentService paymentService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    private PaymentCreateRequest paymentCreateRequest;
//    private PaymentUpdateRequest paymentUpdateRequest;
//    private PaymentView paymentView;
//
//    @BeforeEach
//    public void setUp() {
//
//        paymentCreateRequest = new PaymentCreateRequest(
//                1L,
//                "CREDIT_CARD",
//                100,
//                LocalDateTime.of(2024, 10, 12, 0, 0),
//                CompletionStatus.COMPLETE
//        );
//
//        paymentUpdateRequest = new PaymentUpdateRequest(
//                CompletionStatus.COMPLETE
//        );
//
//        paymentView = new PaymentView(
//                1L,
//                1L,
//                LocalDate.of(2024, 10, 11).atStartOfDay(),
//                LocalDate.of(2024, 10, 13).atStartOfDay(),
//                "CREDIT_CARD",
//                100,
//                CompletionStatus.COMPLETE
//        );
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    public void testProcessPayment() throws Exception {
//
//        // given
//        PaymentCreateRequest expectedRequest = new PaymentCreateRequest(1L, "CREDIT_CARD", 100, LocalDateTime.of(2024, 10, 12, 0, 0), CompletionStatus.COMPLETE);
//
//        // when-then
//        mockMvc.perform(post("/api/v1/payment")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{ \"reservationId\": 1, \"amount\": 100, \"method\": \"CREDIT_CARD\", \"completionStatus\": \"COMPLETE\", \"paymentDate\": \"2024-10-12T00:00:00\" }"))
//                .andExpect(status().isCreated());
//
//        ArgumentCaptor<PaymentCreateRequest> captor = ArgumentCaptor.forClass(PaymentCreateRequest.class);
//        verify(paymentService, times(1)).processReservationPayment(eq(1L), captor.capture());
//
//        assertEquals(expectedRequest.getReservationId(), captor.getValue().getReservationId());
//        assertEquals(expectedRequest.getMethod(), captor.getValue().getMethod());
//        assertEquals(expectedRequest.getAmount(), captor.getValue().getAmount());
//        assertEquals(expectedRequest.getCompletionStatus(), captor.getValue().getCompletionStatus());
//        assertEquals(expectedRequest.getPaymentDate(), captor.getValue().getPaymentDate());
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    public void testGetAllPayments() throws Exception {
//
//        // given
//        List<PaymentView> paymentViewList = Collections.singletonList(paymentView);
//
//        // when
//        when(paymentService.getAllPayments(0, 10)).thenReturn(paymentViewList);
//
//        // then
//        mockMvc.perform(get("/api/v1/payment?page=0&size=10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1));
//
//        verify(paymentService, times(1)).getAllPayments(0, 10);
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    public void testGetPaymentById() throws Exception {
//
//        // given
//        when(paymentService.getPaymentById(1L)).thenReturn(paymentView);
//
//        // when-then
//        mockMvc.perform(get("/api/v1/payment/1"))
//                .andExpect(status().isOk());
//
//        verify(paymentService, times(1)).getPaymentById(1L);
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    public void testCancelPayment() throws Exception {
//
//        // given
//        doNothing().when(paymentService).cancelPayment(any(Long.class), any(PaymentUpdateRequest.class));
//
//        // when-then
//        mockMvc.perform(put("/api/v1/payment/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{ \"paymentDate\": \"2023-10-12\", \"completionStatus\": \"CANCELL\" }"))
//                .andExpect(status().isNoContent());
//
//        verify(paymentService, times(1)).cancelPayment(any(Long.class), any(PaymentUpdateRequest.class));
//    }
//}
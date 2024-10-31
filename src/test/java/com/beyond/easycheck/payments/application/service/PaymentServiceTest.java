package com.beyond.easycheck.payments.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.payments.exception.PaymentMessageType;
import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import com.beyond.easycheck.payments.infrastructure.repository.PaymentRepository;
import com.beyond.easycheck.payments.ui.requestbody.PaymentCreateRequest;
import com.beyond.easycheck.payments.ui.requestbody.PaymentUpdateRequest;
import com.beyond.easycheck.payments.ui.view.PaymentView;
import com.beyond.easycheck.reservationrooms.exception.ReservationRoomMessageType;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.PaymentStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.repository.ReservationRoomRepository;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRoomRepository reservationRoomRepository;

    @Mock
    private PaymentCreateRequest paymentCreateRequest;

    private PaymentEntity paymentEntity;
    private ReservationRoomEntity reservationRoomEntity;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        var roomEntity = mock(RoomEntity.class);
        when(roomEntity.getRoomAmount()).thenReturn(100);

        reservationRoomEntity = mock(ReservationRoomEntity.class);
        when(reservationRoomEntity.getRoomEntity()).thenReturn(roomEntity);
        when(reservationRoomEntity.getCheckinDate()).thenReturn(LocalDate.now());
        when(reservationRoomEntity.getCheckoutDate()).thenReturn(LocalDate.now().plusDays(1));

        paymentEntity = mock(PaymentEntity.class);
        when(paymentEntity.getReservationRoomEntity()).thenReturn(reservationRoomEntity);
        when(paymentEntity.getAmount()).thenReturn(100);
    }

    @Test
    void testProcessReservationPayment() {

        // Given
        Long reservationId = 1L;

        when(reservationRoomEntity.getTotalPrice()).thenReturn(100);

        when(reservationRoomEntity.getReservationStatus()).thenReturn(ReservationStatus.RESERVATION);
        when(reservationRoomEntity.getPaymentStatus()).thenReturn(PaymentStatus.UNPAID);

        when(paymentCreateRequest.getReservationId()).thenReturn(reservationId);
        when(paymentCreateRequest.getMethod()).thenReturn("CARD");
        when(paymentCreateRequest.getAmount()).thenReturn(100);
        when(paymentCreateRequest.getPaymentDate()).thenReturn(LocalDateTime.now());
        when(paymentCreateRequest.getCompletionStatus()).thenReturn(CompletionStatus.COMPLETE);

        when(paymentEntity.getCompletionStatus()).thenReturn(CompletionStatus.COMPLETE);

        when(reservationRoomRepository.findById(reservationId)).thenReturn(Optional.of(reservationRoomEntity));

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);

        // When
        paymentService.processReservationPayment(reservationId, paymentCreateRequest);

        // Then
        verify(reservationRoomRepository, times(1)).save(reservationRoomEntity);
        verify(paymentCreateRequest, times(1)).getReservationId();
        verify(paymentCreateRequest, times(1)).getMethod();
        verify(paymentCreateRequest, times(1)).getAmount();
        verify(paymentCreateRequest, times(1)).getPaymentDate();
        verify(paymentCreateRequest, times(1)).getCompletionStatus();
    }

    @Test
    void testGetAllPayments() {

        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PaymentEntity> paymentPage = new PageImpl<>(Collections.singletonList(paymentEntity));
        when(paymentRepository.findAll(pageRequest)).thenReturn(paymentPage);

        // When
        var payments = paymentService.getAllPayments();

        // Then
        assertEquals(1, payments.size());
        verify(paymentRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void testGetPaymentById() {

        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));

        // When
        PaymentView paymentView = paymentService.getPaymentById(paymentId);

        // Then
        assertNotNull(paymentView);
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void testCancelPayment() {

        // Given
        Long paymentId = 1L;
        PaymentUpdateRequest paymentUpdateRequest = new PaymentUpdateRequest();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));

        // When
        paymentService.cancelPayment(paymentId, paymentUpdateRequest);

        // Then
        verify(paymentRepository, times(1)).save(paymentEntity);
    }

    @Test
    void testProcessReservationPayment_ReservationNotFound() {

        // Given
        Long reservationId = 1L;
        when(reservationRoomRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        EasyCheckException exception = assertThrows(EasyCheckException.class,
                () -> paymentService.processReservationPayment(reservationId, paymentCreateRequest));

        assertEquals(ReservationRoomMessageType.RESERVATION_NOT_FOUND.getMessage(), exception.getMessage());
        verify(reservationRoomRepository, times(1)).findById(reservationId);
    }

    @Test
    void testGetPaymentById_PaymentNotFound() {

        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        EasyCheckException exception = assertThrows(EasyCheckException.class,
                () -> paymentService.getPaymentById(paymentId));

        assertEquals(PaymentMessageType.PAYMENT_NOT_FOUND.getMessage(), exception.getMessage());
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void testCancelPayment_PaymentNotFound() {

        // Given
        Long paymentId = 1L;
        PaymentUpdateRequest paymentUpdateRequest = new PaymentUpdateRequest();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        EasyCheckException exception = assertThrows(EasyCheckException.class,
                () -> paymentService.cancelPayment(paymentId, paymentUpdateRequest));

        assertEquals(PaymentMessageType.PAYMENT_NOT_FOUND.getMessage(), exception.getMessage());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(0)).save(any(PaymentEntity.class));
    }
}
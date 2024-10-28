package com.beyond.easycheck.payments.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.mail.application.service.MailService;
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
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.rooms.infrastructure.entity.DailyRoomAvailabilityEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.DailyRoomAvailabilityRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRoomRepository reservationRoomRepository;
    private final DailyRoomAvailabilityRepository dailyRoomAvailabilityRepository;

    private final MailService mailService;

    private IamportClient iamportClient;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    @Transactional
    public void processReservationPayment(Long reservationId, PaymentCreateRequest paymentCreateRequest) {

        ReservationRoomEntity reservationRoomEntity = reservationRoomRepository.findById(reservationId)
                .orElseThrow(() -> new EasyCheckException(ReservationRoomMessageType.RESERVATION_NOT_FOUND));

        IamportResponse<Payment> paymentResponse = validatePortOnePayment(paymentCreateRequest.getImpUid());

        if (paymentResponse != null && paymentResponse.getResponse().getAmount().intValue() == paymentCreateRequest.getAmount()) {

            if ("vbank".equals(paymentCreateRequest.getMethod())) {
                handleVirtualAccountPayment(paymentCreateRequest, reservationRoomEntity, paymentResponse);
            } else {
                PaymentEntity paymentEntity = createPayment(paymentCreateRequest, reservationRoomEntity);
                paymentRepository.save(paymentEntity);

                reservationRoomEntity.updatePaymentStatus(PaymentStatus.PAID);
                reservationRoomRepository.save(reservationRoomEntity);

                ReservationRoomView reservationRoomView = ReservationRoomView.of(reservationRoomEntity);
                mailService.sendReservationConfirmationEmail(reservationRoomEntity.getUserEntity().getEmail(), reservationRoomView);
            }

        } else {
            throw new EasyCheckException(PaymentMessageType.PAYMENT_VERIFICATION_FAILED);
        }
    }

    @Transactional
    public void handleVirtualAccountDeposit(String impUid) {

        IamportResponse<Payment> paymentResponse = validatePortOnePayment(impUid);

        if (paymentResponse != null && "vbank".equals(paymentResponse.getResponse().getPayMethod())) {
            PaymentEntity paymentEntity = paymentRepository.findByImpUid(impUid)
                    .orElseThrow(() -> new EasyCheckException(PaymentMessageType.PAYMENT_NOT_FOUND));

            paymentEntity.updateCompletionStatus(CompletionStatus.COMPLETE);
            paymentRepository.save(paymentEntity);

            log.info("가상계좌 입금 확인: impUid={}, 결제 완료", impUid);
        }
    }

    public void handleVirtualAccountPayment(PaymentCreateRequest paymentCreateRequest, ReservationRoomEntity reservationRoomEntity, IamportResponse<Payment> paymentIamportResponse) {

        log.info("가상계좌 결제 처리: 은행 = {}, 계좌명 = {}", paymentCreateRequest.getBank(), paymentCreateRequest.getAccountHolder());

        PaymentEntity paymentEntity = createPayment(paymentCreateRequest, reservationRoomEntity);
        paymentEntity.updateCompletionStatus(CompletionStatus.INCOMPLETE);

        log.info("가상계좌 생성됨: {} - {} 은행, 입금자: {}", paymentCreateRequest.getBank(),paymentCreateRequest.getAmount(), paymentCreateRequest.getAccountHolder());
        paymentRepository.save(paymentEntity);
    }

    public IamportResponse<Payment> validatePortOnePayment(String impUid) {

        try {
            IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(impUid);

            if (paymentResponse == null || paymentResponse.getResponse() == null) {
                throw new EasyCheckException(PaymentMessageType.PORTONE_VERIFICATION_ERROR);
            }

            return paymentResponse;
        } catch (IamportResponseException | IOException e) {
            log.error("PortOne 결제 검증 오류: impUid={}, message={}", impUid, e.getMessage());
            throw new EasyCheckException(PaymentMessageType.PORTONE_VERIFICATION_ERROR);
        }
    }

    @Transactional
    public PaymentEntity createPayment(PaymentCreateRequest paymentCreateRequest, ReservationRoomEntity reservationRoomEntity) {

        return PaymentEntity.builder()
                .impUid(paymentCreateRequest.getImpUid())
                .reservationRoomEntity(reservationRoomEntity)
                .method(paymentCreateRequest.getMethod())
                .amount(paymentCreateRequest.getAmount())
                .paymentDate(paymentCreateRequest.getPaymentDate())
                .completionStatus(paymentCreateRequest.getCompletionStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentView> getAllPayments() {

        return paymentRepository.findAll().stream().map(PaymentView::of).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentView getPaymentById(Long id) {

        PaymentEntity paymentEntity = paymentRepository.findById(id).orElseThrow(
                () -> new EasyCheckException(PaymentMessageType.PAYMENT_NOT_FOUND)
        );

        return PaymentView.of(paymentEntity);
    }

    @Transactional
    public void cancelPayment(Long id, PaymentUpdateRequest paymentUpdateRequest) {

        PaymentEntity paymentEntity = paymentRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(PaymentMessageType.PAYMENT_NOT_FOUND));

        try {
            CancelData cancelData = new CancelData(paymentUpdateRequest.getImpUid(), true);
            IamportResponse<Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

            if (cancelResponse == null || cancelResponse.getResponse() == null) {
                throw new EasyCheckException(PaymentMessageType.PORTONE_REFUND_FAILED);
            }

            paymentEntity.updateCompletionStatus(CompletionStatus.REFUND);
            paymentRepository.save(paymentEntity);

            ReservationRoomEntity reservationRoomEntity = paymentEntity.getReservationRoomEntity();
            reservationRoomEntity.updatePaymentStatus(PaymentStatus.UNPAID);
            reservationRoomEntity.updateReservationStatus(ReservationStatus.CANCELED);

            LocalDate checkinDate = reservationRoomEntity.getCheckinDate();
            LocalDate checkoutDate = reservationRoomEntity.getCheckoutDate();

            for (LocalDate date = checkinDate; !date.isEqual(checkoutDate); date = date.plusDays(1)) {
                DailyRoomAvailabilityEntity dailyAvailability = dailyRoomAvailabilityRepository
                        .findByRoomEntityAndDate(reservationRoomEntity.getRoomEntity(), date.atStartOfDay())
                        .orElseThrow(() -> new EasyCheckException(ReservationRoomMessageType.ROOM_NOT_AVAILABLE));
                dailyAvailability.incrementRemainingRoom();

                if (dailyAvailability.getRemainingRoom() <= 0) {
                    dailyAvailability.setStatus(RoomStatus.예약불가);
                } else {
                    dailyAvailability.setStatus(RoomStatus.예약가능);
                }

                dailyRoomAvailabilityRepository.save(dailyAvailability);
            }

            log.info("환불 성공: 결제 ID = {}, 환불 금액 = {}", paymentEntity.getId(), cancelResponse.getResponse().getCancelAmount());

        } catch (IamportResponseException | IOException e) {
            log.error("환불 실패: 결제 ID = {}, 오류 메시지 = {}", paymentEntity.getId(), e.getMessage());
            throw new EasyCheckException(PaymentMessageType.PORTONE_REFUND_FAILED);
        }
    }
}


package com.beyond.easycheck.tickets.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.mail.application.service.MailService;
import com.beyond.easycheck.tickets.exception.TicketOrderMessageType;
import com.beyond.easycheck.tickets.infrastructure.entity.OrderStatus;
import com.beyond.easycheck.tickets.infrastructure.entity.PaymentStatus;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketOrderEntity;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketPaymentEntity;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketOrderRepository;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketPaymentRepository;
import com.beyond.easycheck.tickets.ui.requestbody.TicketPaymentRequest;
import com.beyond.easycheck.tickets.ui.requestbody.TicketPaymentUpdateRequest;
import com.beyond.easycheck.tickets.ui.view.TicketPaymentView;
import com.beyond.easycheck.user.application.service.UserService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.beyond.easycheck.tickets.exception.TicketOrderMessageType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPaymentService {

    private final TicketOrderRepository ticketOrderRepository;
    private final TicketPaymentRepository ticketPaymentRepository;

    private final UserService userService;
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
    public TicketPaymentView processPayment(Long orderId, Long userId, TicketPaymentRequest request) {
        TicketOrderEntity order = getOrderById(orderId);

        validateUserAccess(order, userId);
        validateOrderStatusForPayment(order);

        IamportResponse<Payment> paymentResponse = validatePortOnePayment(request.getImpUid());

        if (portonePaymentFailed(request, paymentResponse)) {
            throw new EasyCheckException(TicketOrderMessageType.PORTONE_VERIFICATION_ERROR);
        }
        TicketPaymentEntity result = null;

        if (paymentMethodVirtualBank(request.getPaymentMethod())) {
            result = handleVirtualAccountTicketPayment(request, order, paymentResponse);
            log.info("handleVirtualAccountTicketPayment result = {}", result);
        } else {
            result = createAndCompletePayment(request, order);
            log.info("createAndCompletePayment result = {}", result);
        }

        ticketPaymentRepository.save(result);

        // 결과 View로 바꿈
        TicketPaymentView ticketPaymentView = new TicketPaymentView(
                result.getId(),
                result.getImpUid(),
                result.getTicketOrder().getId(),
                result.getTicketOrder().getUserEntity().getId(),
                result.getTicketOrder().getUserEntity().getName(),
                result.getTicketOrder().getUserEntity().getPoint(),
                result.getTicketOrder().getTicket().getThemePark().getAccommodation().getName(),
                result.getTicketOrder().getTicket().getTicketName(),
                result.getTicketOrder().getTicket().getThemePark().getName(),
                result.getTicketOrder().getTicket().getValidFromDate(),
                result.getTicketOrder().getTicket().getValidToDate(),
                result.getTicketOrder().getQuantity(),
                result.getPaymentStatus(),
                result.getCancelDate(),
                result.getPaymentMethod(),
                result.getPaymentAmount(),
                result.getPaymentDate()
        );

        // 포인트 적립
        BigDecimal paymentAmount = request.getPaymentAmount();
        BigDecimal pointsPercentage = new BigDecimal("0.04");

        int pointsToAccumulate = paymentAmount.multiply(pointsPercentage)
                .setScale(0, RoundingMode.FLOOR)
                .intValue();

        userService.accumulatePoints(pointsToAccumulate);

        // 티켓 구매 완료 이메일 전송
        mailService.sendTicketPaymentConfirmationEmail(result.getTicketOrder().getUserEntity().getEmail(), ticketPaymentView);

        return ticketPaymentView;


    }

    public TicketPaymentEntity createAndCompletePayment(TicketPaymentRequest ticketPaymentRequest, TicketOrderEntity ticketOrderEntity) {
        log.info("createAndCompletePayment ticketPaymentRequest = {}", ticketPaymentRequest);
        return TicketPaymentEntity.builder()
                .ticketOrder(ticketOrderEntity)
                .impUid(ticketPaymentRequest.getImpUid())
                .paymentAmount(ticketPaymentRequest.getPaymentAmount())
                .paymentMethod(ticketPaymentRequest.getPaymentMethod())
                .bank(ticketPaymentRequest.getBank())
                .accountHolder(ticketPaymentRequest.getAccountHolder())
                .depositDeadline(ticketPaymentRequest.getDepositDeadline())
                .paymentStatus(ticketPaymentRequest.getPaymentStatus() != null ? ticketPaymentRequest.getPaymentStatus() : PaymentStatus.COMPLETED)
                .build();
    }

    public IamportResponse<Payment> validatePortOnePayment(String impUid) {
        try {
            IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(impUid);

            if (Objects.isNull(paymentResponse) || Objects.isNull(paymentResponse.getResponse())) {
                throw new EasyCheckException(TicketOrderMessageType.PORTONE_VERIFICATION_ERROR);
            }

            return paymentResponse;
        } catch (IamportResponseException | IOException e) {
            log.error("IamPort 결제 검증 오류: impUid={}, message={}", impUid, e.getMessage());
            throw new EasyCheckException(TicketOrderMessageType.PORTONE_VERIFICATION_ERROR);
        }
    }

    @Transactional
    public void cancelPayment(Long id, TicketPaymentUpdateRequest ticketPaymentUpdateRequest) {

        TicketPaymentEntity ticketPaymentEntity = ticketPaymentRepository.findById(id)
                .orElse(null);

        if (ticketPaymentEntity == null) {
            throw new EasyCheckException(TicketOrderMessageType.PAYMENT_NOT_FOUND);
        } else {
        }

        try {
            CancelData cancelData = new CancelData(ticketPaymentUpdateRequest.getImpUid(), true);
            IamportResponse<Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

            if (cancelResponse == null || cancelResponse.getResponse() == null) {
                throw new EasyCheckException(TicketOrderMessageType.PORTONE_REFUND_FAILED);
            }

            ticketPaymentEntity.updatePaymentStatus(PaymentStatus.REFUNDED);
            ticketPaymentRepository.save(ticketPaymentEntity);

            TicketOrderEntity ticketOrderEntity = ticketPaymentEntity.getTicketOrder();
            ticketOrderEntity.updateOrderStatus(OrderStatus.CANCELLED);

        } catch (IamportResponseException | IOException e) {
            throw new EasyCheckException(TicketOrderMessageType.PORTONE_REFUND_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<TicketPaymentView> getAllTicketPayments() {

        return ticketPaymentRepository.findAll().stream().map(TicketPaymentView::of).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketPaymentEntity getPaymentStatus(Long orderId) {
        return ticketPaymentRepository.findByTicketOrderId(orderId)
                .orElseThrow(() -> new EasyCheckException(PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<TicketPaymentEntity> getPaymentHistory(Long userId) {
        return ticketPaymentRepository.findAllByTicketOrder_UserEntity_Id(userId);
    }

    private boolean paymentMethodVirtualBank(String paymentMethod) {
        return "vbank".equals(paymentMethod);
    }

    private boolean portonePaymentFailed(TicketPaymentRequest request, IamportResponse<Payment> paymentResponse) {
        log.info("request = {} paymentResponse = ${}", request, paymentResponse);
        return paymentResponse == null && paymentResponse.getResponse().getAmount().compareTo(request.getPaymentAmount()) != 0;
    }

    public TicketPaymentEntity handleVirtualAccountTicketPayment(TicketPaymentRequest request, TicketOrderEntity order, IamportResponse<Payment> paymentResponse) {

        log.info("가상계좌 결제 처리: 은행 = {}, 계좌명 = {}", request.getBank(), request.getAccountHolder());

        TicketPaymentEntity ticketPaymentEntity = createAndCompletePayment(request, order);
        ticketPaymentEntity.updatePaymentStatus(PaymentStatus.PENDING);

        log.info("가상계좌 생성됨: {} - {} 은행, 입금자: {}", request.getBank(), request.getPaymentAmount(), request.getAccountHolder());
        return ticketPaymentRepository.save(ticketPaymentEntity);
    }


    private TicketOrderEntity getOrderById(Long orderId) {
        return ticketOrderRepository.findById(orderId)
                .orElseThrow(() -> new EasyCheckException(ORDER_NOT_FOUND));
    }

    private void validateUserAccess(TicketOrderEntity order, Long userId) {
        if (!order.getUserEntity().getId().equals(userId)) {
            throw new EasyCheckException(UNAUTHORIZED_ACCESS);
        }
    }

    private void validateOrderStatusForPayment(TicketOrderEntity order) {
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new EasyCheckException(INVALID_ORDER_STATUS_FOR_PAYMENT);
        }
    }

    private void validateOrderStatusForCancellation(TicketOrderEntity order) {
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new EasyCheckException(ORDER_ALREADY_CANCELLED);
        }
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new EasyCheckException(ORDER_ALREADY_COMPLETED);
        }
    }

    private void handlePaymentException(Exception e, TicketOrderEntity order) {
        log.error("주문 ID: {} 결제 실패, 사유: {}", order.getId(), e.getMessage());
        throw new EasyCheckException(PAYMENT_FAILED);
    }
}
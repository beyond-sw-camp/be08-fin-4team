package com.beyond.easycheck.tickets.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketOrderEntity;
import com.beyond.easycheck.tickets.infrastructure.entity.OrderStatus;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketPaymentEntity;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketOrderRepository;
import com.beyond.easycheck.tickets.infrastructure.entity.TicketEntity;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketPaymentRepository;
import com.beyond.easycheck.tickets.infrastructure.repository.TicketRepository;
import com.beyond.easycheck.tickets.ui.requestbody.TicketOrderRequest;
import com.beyond.easycheck.tickets.ui.view.TicketOrderDTO;
import com.beyond.easycheck.tickets.ui.view.OrderDetailsDTO;
import com.beyond.easycheck.user.exception.UserMessageType;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.beyond.easycheck.tickets.exception.TicketMessageType.*;
import static com.beyond.easycheck.tickets.exception.TicketOrderMessageType.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketOrderService implements TicketOrderOperationUseCase, TicketOrderReadUseCase {

    private final TicketOrderRepository ticketOrderRepository;
    private final TicketPaymentRepository ticketPaymentRepository;
    private final TicketRepository ticketRepository;
    private final UserJpaRepository userJpaRepository;

    @Transactional
    public TicketOrderDTO createTicketOrder(Long userId, TicketOrderRequest request) {

        UserEntity userEntity = getUserById(userId);
        log.info(userEntity.toString());
        TicketEntity ticket = getTicketById(request.getTicketId());
        log.info(ticket.toString());
        validateSalePeriod(ticket);
        validateQuantity(request.getQuantity());
        log.info("여기 나오나?");
        TicketOrderEntity ticketOrder = new TicketOrderEntity(
                userEntity,
                request.getCollectionAgreement()
        );

        for (int i = 0; i < tickets.size(); i++) {
            TicketEntity ticket = tickets.get(i);
            int quantity = request.getQuantities().get(i);
            ticketOrder.addOrderDetail(ticket, quantity);
        }

        ticketOrderRepository.save(ticketOrder);

        return convertToDTO(ticketOrder, null);
    }

    @Override
    @Transactional
    public void cancelTicketOrder(Long userId, Long orderId) {
        TicketOrderEntity ticketOrder = getTicketOrderByIdAndUserId(userId, orderId);

        if (ticketOrder.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new EasyCheckException(ORDER_ALREADY_CANCELLED);
        }

        ticketOrder.cancelOrder();
        ticketOrderRepository.save(ticketOrder);
    }

    @Transactional
    public void completeOrder(Long userId, Long orderId) {
        TicketOrderEntity order = getTicketOrderByIdAndUserId(userId, orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new EasyCheckException(INVALID_ORDER_STATUS_FOR_COMPLETION);
        }

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new EasyCheckException(ORDER_ALREADY_COMPLETED);
        }

        order.completeOrder();
        ticketOrderRepository.save(order);
    }

    @Override
    public TicketOrderDTO getTicketOrder(Long userId, Long orderId) {
        TicketOrderEntity ticketOrder = getTicketOrderByIdAndUserId(userId, orderId);
        TicketPaymentEntity payment = ticketPaymentRepository.findByTicketOrderId(orderId).orElse(null);

        return convertToDTO(ticketOrder, payment);
    }

    @Override
    public List<TicketOrderDTO> getAllOrdersByUserId(Long userId) {
        List<TicketOrderEntity> orders = ticketOrderRepository.findByUserEntity_Id(userId);
        return orders.stream().map(order -> convertToDTO(order, null)).collect(Collectors.toList());
    }

    private TicketOrderEntity getTicketOrderByIdAndUserId(Long userId, Long orderId) {
        TicketOrderEntity ticketOrder = ticketOrderRepository.findById(orderId)
                .orElseThrow(() -> new EasyCheckException(ORDER_NOT_FOUND));
        if (!ticketOrder.getUserEntity().getId().equals(userId)) {
            throw new EasyCheckException(UNAUTHORIZED_ACCESS);
        }
        return ticketOrder;
    }

    private UserEntity getUserById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));
    }

    private List<TicketEntity> getTicketsByIds(List<Long> ticketIds) {
        List<TicketEntity> tickets = ticketRepository.findAllById(ticketIds);
        if (tickets.size() != ticketIds.size()) {
            throw new EasyCheckException(TICKET_NOT_FOUND);
        }
        return tickets;
    }

    private void validateTickets(List<TicketEntity> tickets, List<Integer> quantities) {
        if (tickets.size() != quantities.size()) {
            throw new EasyCheckException(INVALID_QUANTITY);
        }

        LocalDateTime now = LocalDateTime.now();
        for (TicketEntity ticket : tickets) {
            if (now.isBefore(ticket.getSaleStartDate()) || now.isAfter(ticket.getSaleEndDate())) {
                throw new EasyCheckException(TICKET_SALE_PERIOD_INVALID);
            }
        }

        for (int quantity : quantities) {
            if (quantity <= 0) {
                throw new EasyCheckException(INVALID_QUANTITY);
            }
        }
    }

    private TicketOrderDTO convertToDTO(TicketOrderEntity ticketOrder, TicketPaymentEntity payment) {
        List<OrderDetailsDTO> orderDetailsDTOList = ticketOrder.getOrderDetails().stream()
                .map(detail -> new OrderDetailsDTO(
                        detail.getTicket().getId(),
                        detail.getTicket().getTicketName(),
                        detail.getQuantity(),
                        detail.getPrice()))
                .collect(Collectors.toList());

        return new TicketOrderDTO(
                ticketOrder.getId(),
                ticketOrder.getUserEntity().getId(),
                ticketOrder.getCollectionAgreement().name(),
                ticketOrder.getOrderStatus(),
                ticketOrder.getTotalPrice(),
                ticketOrder.getPurchaseTimestamp(),
                orderDetailsDTOList,
                payment != null ? payment.getPaymentMethod() : null,
                payment != null ? payment.getPaymentAmount() : null
        );
    }

}

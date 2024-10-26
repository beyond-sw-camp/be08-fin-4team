package com.beyond.easycheck.reservationrooms.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.reservationrooms.exception.ReservationRoomMessageType;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.repository.ReservationRoomRepository;
import com.beyond.easycheck.reservationrooms.ui.requestbody.ReservationRoomCreateRequest;
import com.beyond.easycheck.reservationrooms.ui.requestbody.ReservationRoomUpdateRequest;
import com.beyond.easycheck.reservationrooms.ui.view.DayRoomAvailabilityView;
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.reservationrooms.ui.view.RoomAvailabilityView;
import com.beyond.easycheck.reservationservices.infrastructure.entity.ReservationServiceEntity;
import com.beyond.easycheck.reservationservices.infrastructure.entity.ReservationServiceStatus;
import com.beyond.easycheck.reservationservices.infrastructure.repository.ReservationServiceRepository;
import com.beyond.easycheck.reservationservices.ui.requestbody.ReservationServiceUpdateRequest;
import com.beyond.easycheck.rooms.exception.RoomMessageType;
import com.beyond.easycheck.rooms.infrastructure.entity.DailyRoomAvailabilityEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.DailyRoomAvailabilityRepository;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.user.exception.UserMessageType;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.UserJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Slf4j
public class ReservationRoomService {

    private final ReservationRoomRepository reservationRoomRepository;
    private final RoomRepository roomRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReservationServiceRepository reservationServiceRepository;
    private final DailyRoomAvailabilityRepository dailyRoomAvailabilityRepository;

    @Transactional
    public ReservationRoomEntity createReservation(Long userId, ReservationRoomCreateRequest reservationRoomCreateRequest) {

        UserEntity userEntity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));

        RoomEntity roomEntity = roomRepository.findById(reservationRoomCreateRequest.getRoomId())
                .orElseThrow(() -> new EasyCheckException(RoomMessageType.ROOM_NOT_FOUND));

        LocalDate checkinDate = reservationRoomCreateRequest.getCheckinDate();
        LocalDate checkoutDate = reservationRoomCreateRequest.getCheckoutDate();

        for (LocalDateTime date = checkinDate.atStartOfDay(); !date.isAfter(checkoutDate.atStartOfDay()); date = date.plusDays(1)) {
            log.info("Logging checkpoint before dailyAvailability retrieval");
            DailyRoomAvailabilityEntity dailyAvailability = dailyRoomAvailabilityRepository
                    .findByRoomEntityAndDate(roomEntity, date)
                    .orElseThrow(() -> new EasyCheckException(ReservationRoomMessageType.ROOM_NOT_AVAILABLE));
            log.info("dailyAvailability: {}, roomEntity: {}", dailyAvailability, roomEntity);

            if (dailyAvailability.getRemainingRoom() <= 0) {
                throw new EasyCheckException(ReservationRoomMessageType.ROOM_ALREADY_FULL);
            }
        }

        ReservationRoomEntity reservationRoomEntity = ReservationRoomEntity.builder()
                .roomEntity(roomEntity)
                .userEntity(userEntity)
                .reservationDate(LocalDateTime.now())
                .checkinDate(LocalDate.from(checkinDate.atTime(15, 0)))
                .checkoutDate(LocalDate.from(checkoutDate.atTime(11, 0)))
                .reservationStatus(ReservationStatus.RESERVATION)
                .totalPrice(reservationRoomCreateRequest.getTotalPrice())
                .paymentStatus(reservationRoomCreateRequest.getPaymentStatus())
                .build();

        reservationRoomRepository.save(reservationRoomEntity);

        for (LocalDateTime date = checkinDate.atStartOfDay(); !date.isAfter(checkoutDate.atStartOfDay()); date = date.plusDays(1)) {

            if (date.isBefore(checkoutDate.atStartOfDay())) {

                DailyRoomAvailabilityEntity dailyAvailability = dailyRoomAvailabilityRepository
                        .findByRoomEntityAndDate(roomEntity, date)
                        .orElseThrow(() -> new EasyCheckException(ReservationRoomMessageType.ROOM_NOT_AVAILABLE));

                dailyAvailability.decrementRemainingRoom();

                if (dailyAvailability.getRemainingRoom() <= 0) {
                    dailyAvailability.setStatus(RoomStatus.예약불가);
                }

                dailyRoomAvailabilityRepository.save(dailyAvailability);
            }
        }

        return reservationRoomEntity;
    }

    @Transactional(readOnly = true)
    public List<RoomAvailabilityView> getAvailableRoomsByCheckInCheckOut(Long accommodationId, LocalDate checkinDate, LocalDate checkoutDate) {

        List<DailyRoomAvailabilityEntity> availableRoomsByDateRange = dailyRoomAvailabilityRepository.findAvailabilityByDateRange(
                checkinDate.atStartOfDay(),
                checkoutDate.atTime(23, 59)
        );
        log.info(availableRoomsByDateRange.toString());

        Map<Long, DailyRoomAvailabilityEntity> uniqueRoomAvailabilityMap = availableRoomsByDateRange.stream()
                .filter(availability ->
                        availability.getRoomEntity().getRoomTypeEntity().getAccommodationEntity().getId().equals(accommodationId) &&
                                availability.getStatus() == RoomStatus.예약가능
                )
                .collect(Collectors.toMap(
                        availability -> availability.getRoomEntity().getRoomId(),
                        availability -> availability,
                        (existing, replacement) -> existing.getRemainingRoom() <= replacement.getRemainingRoom() ? existing : replacement
                ));

        log.info(uniqueRoomAvailabilityMap.toString());
        List<RoomAvailabilityView> availableRooms = uniqueRoomAvailabilityMap.values().stream()
                .map(availability -> {
                    RoomEntity roomEntity = availability.getRoomEntity();

                    List<String> imageUrls = roomEntity.getImages().stream()
                            .map(RoomEntity.ImageEntity::getUrl)
                            .collect(Collectors.toList());

                    return new RoomAvailabilityView(
                            roomEntity.getRoomId(),
                            roomEntity.getRoomTypeEntity().getTypeName(),
                            roomEntity.getRoomNumber(),
                            availability.getRemainingRoom(),
                            availability.getStatus(),
                            imageUrls
                    );
                })
                .collect(Collectors.toList());
        log.info(availableRooms.toString());
        return availableRooms;
    }

    @Transactional(readOnly = true)
    public List<DayRoomAvailabilityView> getAvailableRoomsByMonth(int year, int month) {

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<DailyRoomAvailabilityEntity> availabilities = dailyRoomAvailabilityRepository
                .findAvailabilityByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59));

        List<RoomEntity> allRooms = roomRepository.findAll();

        return createDayRoomAvailabilityViews(availabilities, startDate, endDate, allRooms);
    }

    private List<DayRoomAvailabilityView> createDayRoomAvailabilityViews(
            List<DailyRoomAvailabilityEntity> availabilities, LocalDate startDate, LocalDate endDate, List<RoomEntity> allRooms) {

        List<DayRoomAvailabilityView> result = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate finalDate = date;

            List<RoomAvailabilityView> rooms = allRooms.stream()
                    .map(room -> {
                        DailyRoomAvailabilityEntity dailyAvailability = availabilities.stream()
                                .filter(availability -> availability.getRoomEntity().equals(room) &&
                                        availability.getDate().toLocalDate().equals(finalDate))
                                .findFirst()
                                .orElse(DailyRoomAvailabilityEntity.builder()
                                        .roomEntity(room)
                                        .date(finalDate.atStartOfDay())
                                        .remainingRoom(room.getRoomAmount())
                                        .status(RoomStatus.예약가능)
                                        .build());

                        List<String> imageUrls = room.getImages().stream()
                                .map(RoomEntity.ImageEntity::getUrl)
                                .collect(Collectors.toList());

                        return new RoomAvailabilityView(
                                room.getRoomId(),
                                room.getRoomTypeEntity().getTypeName(),
                                room.getRoomNumber(),
                                dailyAvailability.getRemainingRoom(),
                                dailyAvailability.getStatus(),
                                imageUrls
                        );
                    })
                    .collect(Collectors.toList());

            result.add(new DayRoomAvailabilityView(
                    finalDate,
                    finalDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN),
                    rooms
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<ReservationRoomEntity> getReservationsForReminder() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        return reservationRoomRepository.findByReservationDateBeforeAndReminderSentFalse(tenMinutesAgo);
    }

    @Transactional(readOnly = true)
    public List<ReservationRoomView> getAllReservations(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReservationRoomEntity> reservationPage = reservationRoomRepository.findAll(pageable);

        return reservationPage.getContent().stream()
                .map(ReservationRoomView::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationRoomView getReservationById(Long id) {

        ReservationRoomEntity reservationRoomEntity = reservationRoomRepository.findById(id).orElseThrow(
                () -> new EasyCheckException(ReservationRoomMessageType.RESERVATION_NOT_FOUND)
        );

        return ReservationRoomView.of(reservationRoomEntity);
    }

    @Transactional
    public void cancelReservation(Long reservationId, ReservationRoomUpdateRequest reservationRoomUpdateRequest) {

        ReservationRoomEntity reservationRoomEntity = reservationRoomRepository.findById(reservationId)
                .orElseThrow(() -> new EasyCheckException(ReservationRoomMessageType.RESERVATION_NOT_FOUND));

        reservationRoomEntity.updateReservationRoom(reservationRoomUpdateRequest);

        List<ReservationServiceEntity> additionalServices = reservationServiceRepository.findByReservationRoomEntity(reservationRoomEntity);
        for (ReservationServiceEntity service : additionalServices) {
            service.cancelReservationService(new ReservationServiceUpdateRequest(ReservationServiceStatus.CANCELED));
        }
        reservationServiceRepository.saveAll(additionalServices);

        LocalDate checkinDate = reservationRoomEntity.getCheckinDate();
        LocalDate checkoutDate = reservationRoomEntity.getCheckoutDate();

        for (LocalDate date = checkinDate; !date.isAfter(checkoutDate); date = date.plusDays(1)) {
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
    }
}


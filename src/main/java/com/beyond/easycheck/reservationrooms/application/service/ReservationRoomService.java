package com.beyond.easycheck.reservationrooms.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.reservationrooms.exception.ReservationRoomMessageType;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationStatus;
import com.beyond.easycheck.reservationrooms.infrastructure.repository.ReservationRoomRepository;
import com.beyond.easycheck.reservationrooms.ui.requestbody.ReservationRoomCreateRequest;
import com.beyond.easycheck.reservationrooms.ui.requestbody.ReservationRoomUpdateRequest;
import com.beyond.easycheck.reservationrooms.ui.view.AvailableReservationRoomView;
import com.beyond.easycheck.reservationrooms.ui.view.DayRoomAvailabilityView;
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.reservationrooms.ui.view.RoomAvailabilityView;
import com.beyond.easycheck.reservationservices.infrastructure.entity.ReservationServiceEntity;
import com.beyond.easycheck.reservationservices.infrastructure.entity.ReservationServiceStatus;
import com.beyond.easycheck.reservationservices.infrastructure.repository.ReservationServiceRepository;
import com.beyond.easycheck.reservationservices.ui.requestbody.ReservationServiceUpdateRequest;
import com.beyond.easycheck.roomrates.application.dto.RoomRateFindQuery;
import com.beyond.easycheck.roomrates.infrastructure.entity.RoomRateEntity;
import com.beyond.easycheck.roomrates.infrastructure.repository.RoomRateRepository;
import com.beyond.easycheck.rooms.exception.RoomMessageType;
import com.beyond.easycheck.rooms.infrastructure.entity.DailyRoomAvailabilityEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.beyond.easycheck.rooms.infrastructure.repository.DailyRoomAvailabilityRepository;
import com.beyond.easycheck.rooms.infrastructure.repository.RoomRepository;
import com.beyond.easycheck.seasons.application.domain.DayType;
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

import java.math.BigDecimal;
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

    private final RoomRepository roomRepository;

    private final UserJpaRepository userJpaRepository;

    private final RoomRateRepository roomRateRepository;

    private final ReservationRoomRepository reservationRoomRepository;

    private final ReservationServiceRepository reservationServiceRepository;

    private final DailyRoomAvailabilityRepository dailyRoomAvailabilityRepository;

    @Transactional
    public ReservationRoomEntity createReservation(Long userId, ReservationRoomCreateRequest request) {

        UserEntity userEntity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));

        RoomEntity roomEntity = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EasyCheckException(RoomMessageType.ROOM_NOT_FOUND));

        LocalDate checkinDate = request.getCheckinDate();
        LocalDate checkoutDate = request.getCheckoutDate();

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
                .totalPrice(request.getTotalPrice())
                .paymentStatus(request.getPaymentStatus())
                // 추가된 부분
                .representativeName(request.getRepresentativeName())
                .representativePhone(request.getRepresentativePhone())
                .representativeEmail(request.getRepresentativeEmail())
                .adultCount(request.getAdultCount())
                .childCount(request.getChildCount())
                .totalRoomCount(request.getTotalRoomCount())
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
    public List<AvailableReservationRoomView> getAvailableRoomsByCheckInCheckOut(Long accommodationId, LocalDate checkinDate, LocalDate checkoutDate) {
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

        // 체크인 날짜를 이용하여 해당하는 방의 모든 요금을 조회
        // 체크인 날짜의 요일이 토요일 또는 일요일인지 확인
        // 요일과 회원 타입에 맞는 가격 찾기

        return uniqueRoomAvailabilityMap.values().stream()
                .map(availability -> {
                    RoomEntity roomEntity = availability.getRoomEntity();

                    List<String> imageUrls = roomEntity.getImages().stream()
                            .map(RoomEntity.ImageEntity::getUrl)
                            .collect(Collectors.toList());

                    String thumbnailImgUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);

                    // 체크인 날짜를 이용하여 해당하는 방의 모든 요금을 조회
                    RoomRateFindQuery query = new RoomRateFindQuery(
                            roomEntity.getRoomId(),
                            checkinDate
                    );

                    List<RoomRateEntity> roomRates = roomRateRepository.findAllRoomRates(query);

                    // 체크인 날짜의 요일이 토요일 또는 일요일인지 확인
                    boolean isWeekend = checkinDate.getDayOfWeek().getValue() >= 6;

                    // 요일과 회원 타입에 맞는 가격 찾기
                    BigDecimal normalPrice = roomRates.stream()
                            .filter(rate ->
                                    rate.getUserType().equals("일반") &&
                                            ((isWeekend && rate.getSeasonEntity().getDayType() == DayType.WEEKEND) ||
                                                    (!isWeekend && rate.getSeasonEntity().getDayType() == DayType.WEEKDAY))
                            )
                            .findFirst()
                            .map(RoomRateEntity::getRate)
                            .orElse(BigDecimal.ZERO);

                    BigDecimal corpPrice = roomRates.stream()
                            .filter(rate ->
                                    rate.getUserType().equals("법인") &&
                                            ((isWeekend && rate.getSeasonEntity().getDayType() == DayType.WEEKEND) ||
                                                    (!isWeekend && rate.getSeasonEntity().getDayType() == DayType.WEEKDAY))
                            )
                            .findFirst()
                            .map(RoomRateEntity::getRate)
                            .orElse(BigDecimal.ZERO);

                    return new AvailableReservationRoomView(
                            roomEntity.getRoomId(),
                            roomEntity.getType(),
                            roomEntity.getRoomTypeEntity().getName(),
                            availability.getRemainingRoom(),
                            availability.getStatus(),
                            roomEntity.getDescription(),
                            thumbnailImgUrl,
                            normalPrice,
                            corpPrice,
                            roomEntity.getMaxOccupancy(),
                            roomEntity.getStandardOccupancy()
                    );
                })
                .collect(Collectors.toList());
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
                                room.getRoomTypeEntity().getName(),
                                room.getType(),
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

//    public List<ReservationRoomEntity> getReservationsForReminder() {
//        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
//        List<ReservationRoomEntity> reservations = reservationRoomRepository.findByReservationDateBeforeAndReminderSentFalse(tenMinutesAgo);
//        return reservations;
//    }

    @Transactional(readOnly = true)
    public List<ReservationRoomEntity> get3DaysBeforeReservationsForReminder() {
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        return reservationRoomRepository.findByCheckinDate(threeDaysFromNow);
    }

    @Transactional(readOnly = true)
    public List<ReservationRoomEntity> get10DaysBeforeReservationsForReminder() {
        LocalDate tenDaysFromNow = LocalDate.now().plusDays(10);
        return reservationRoomRepository.findByCheckinDate(tenDaysFromNow);
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


package com.beyond.easycheck.admin.application.service;

import com.beyond.easycheck.additionalservices.infrastructure.entity.AdditionalServiceEntity;
import com.beyond.easycheck.attractions.infrastructure.entity.AttractionEntity;
import com.beyond.easycheck.events.infrastructure.entity.EventEntity;
import com.beyond.easycheck.facilities.infrastructure.entity.AvailableStatus;
import com.beyond.easycheck.facilities.infrastructure.entity.FacilityEntity;
import com.beyond.easycheck.notices.infrastructure.persistence.entity.NoticesEntity;
import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import com.beyond.easycheck.suggestion.infrastructure.persistence.entity.SuggestionEntity;
import com.beyond.easycheck.themeparks.infrastructure.entity.ThemeParkEntity;
import com.beyond.easycheck.user.application.service.UserReadUseCase.FindUserResult;
import com.beyond.easycheck.user.application.service.UserReadUseCase.UserFindQuery;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminReadUseCase {

    List<FindUserResult> getAllUsers(UserFindQuery query);

    // 단일 유저 조회
    FindUserResult getUserDetails(UserFindQuery query);

    // 현재 로그인한 관리자 정보 조회
    FindAdminResult getAdminDetails();

    List<FindEventResult> getAllEvents();

    List<FindNoticeResult> getAllNotices();

    List<FindPaymentResult> getAllPayments(PaymentFindQuery query);

    List<FindThemeParkResult> getAllThemeParks();

    List<FindFacilitiesResult> getAllFacilities();

    List<FindSuggestionResult> getAllSuggestions();

    List<FindAttractionResult> getAllAttractions();

    List<FindAdditionalServiceResult> getAllAdditionalServices();

    record PaymentFindQuery(
            Long paymentId,
            String userName,
            String email
    ) {
    }

    record FindNoticeResult(
            Long id,
            String accommodationName,
            String userName,
            String title,
            String content
    ) {
        public static FindNoticeResult findByNoticeWithUserAndAccommodation(NoticesEntity notices) {
            return new FindNoticeResult(
                    notices.getId(),
                    notices.getAccommodationEntity().getName(),
                    notices.getUserEntity().getName(),
                    notices.getTitle(),
                    notices.getContent()
            );
        }
    }

    record FindThemeParkResult(
            Long id,
            String name,
            String description,
            List<String> images
    ) {
        static public FindThemeParkResult findByThemeParkEntity(ThemeParkEntity themePark) {
            return new FindThemeParkResult(
                    themePark.getId(),
                    themePark.getName(),
                    themePark.getDescription(),
                    themePark.getImages().stream().map(ThemeParkEntity.ImageEntity::getUrl).toList(
                    ));
        }
    }

    record FindEventResult(
            Long id,
            String accommodationName,
            List<String> images,
            String eventName,
            String detail,
            LocalDate startDate,
            LocalDate endDate

    ) {
        public static FindEventResult findByEvent(EventEntity event) {
            return new FindEventResult(
                    event.getId(),
                    event.getAccommodationEntity().getName(),
                    event.getImages().stream().map(EventEntity.ImageEntity::getUrl).toList(),
                    event.getEventName(),
                    event.getDetail(),
                    event.getStartDate(),
                    event.getEndDate()
            );
        }
    }

    record FindSuggestionResult(
            Long id,
            String type,
            String subject,
            String email,
            String title,
            String content,
            FindUserResult user
    ) {
        public static FindSuggestionResult findBySuggestionEntity(SuggestionEntity suggestion) {
            return new FindSuggestionResult(
                    suggestion.getId(),
                    suggestion.getType(),
                    suggestion.getSubject(),
                    suggestion.getEmail(),
                    suggestion.getTitle(),
                    suggestion.getContent(),
                    suggestion.getUserEntity() == null ?
                            null : FindUserResult.findByUserEntity(suggestion.getUserEntity()
                    )
            );
        }


    }

    record FindAttractionResult(
            Long id,
            String name,
            String introduction,
            String information,
            String standardUse,
            Long themeParkId,
            String imageUrl
    ) {
        static public FindAttractionResult findByAttractionEntity(AttractionEntity attraction) {
            return new FindAttractionResult(
                    attraction.getId(),
                    attraction.getName(),
                    attraction.getIntroduction(),
                    attraction.getInformation(),
                    attraction.getStandardUse(),
                    attraction.getThemePark().getId(),
                    attraction.getImageUrl()
            );
        }
    }

    record FindFacilitiesResult(
            Long id,
            String name,
            String description,
            List<String> images,
            String accommodationName,
            AvailableStatus availableStatus
    ) {
        static public FindFacilitiesResult findByFacilityEntity(FacilityEntity facility) {
            return new FindFacilitiesResult(
                    facility.getId(),
                    facility.getName(),
                    facility.getDescription(),
                    facility.getImages().stream().map(FacilityEntity.ImageEntity::getUrl).toList(),
                    facility.getAccommodationEntity().getName(),
                    facility.getAvailableStatus()
            );
        }
    }

    record FindAdditionalServiceResult(
            Long id,
            String name,
            String description,
            Integer price
    ) {
        public static FindAdditionalServiceResult findByAdditionalServiceEntity(AdditionalServiceEntity additionalService) {
            return new FindAdditionalServiceResult(
                    additionalService.getId(),
                    additionalService.getName(),
                    additionalService.getDescription(),
                    additionalService.getPrice()
            );
        }
    }

    record FindPaymentResult(
            Long id,
            String impUid,
            String username,
            String email,
            String userRole,
            Long reservationRoomId,
            LocalDateTime checkinDate,
            LocalDateTime checkoutDate,
            LocalDateTime paymentDate,
            String method,
            Integer amount,
            CompletionStatus completionStatus
    ) {
        static public FindPaymentResult findByPaymentEntity(PaymentEntity payment) {
            return new FindPaymentResult(
                    payment.getId(),
                    payment.getImpUid(),
                    payment.getReservationRoomEntity().getUserEntity().getName(),
                    payment.getReservationRoomEntity().getUserEntity().getEmail(),
                    payment.getReservationRoomEntity().getUserEntity().getRole().getName(),
                    payment.getReservationRoomEntity().getRoomEntity().getRoomId(),
                    payment.getReservationRoomEntity().getCheckinDate().atStartOfDay(),
                    payment.getReservationRoomEntity().getCheckoutDate().atStartOfDay(),
                    payment.getPaymentDate(),
                    payment.getMethod(),
                    payment.getAmount(),
                    payment.getCompletionStatus()
            );
        }
    }

    record FindAdminResult() {
    }

}

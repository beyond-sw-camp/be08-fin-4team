package com.beyond.easycheck.admin.application.service;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.additionalservices.infrastructure.repository.AdditionalServiceRepository;
import com.beyond.easycheck.admin.exception.AdminMessageType;
import com.beyond.easycheck.admin.infrastructure.persistence.mariadb.repository.payment.PaymentJpaRepository;
import com.beyond.easycheck.attractions.infrastructure.repository.AttractionRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.common.security.utils.JwtUtil;
import com.beyond.easycheck.events.infrastructure.repository.EventRepository;
import com.beyond.easycheck.facilities.infrastructure.repository.FacilityRepository;
import com.beyond.easycheck.notices.infrastructure.persistence.repository.NoticesRepository;
import com.beyond.easycheck.suggestion.infrastructure.persistence.repository.SuggestionsRepository;
import com.beyond.easycheck.themeparks.infrastructure.repository.ThemeParkRepository;
import com.beyond.easycheck.user.application.domain.EasyCheckUserDetails;
import com.beyond.easycheck.user.application.domain.UserRole;
import com.beyond.easycheck.user.exception.UserMessageType;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.beyond.easycheck.user.application.service.UserReadUseCase.FindUserResult;
import static com.beyond.easycheck.user.application.service.UserReadUseCase.UserFindQuery;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService implements AdminOperationUseCase, AdminReadUseCase {

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    private final EventRepository eventRepository;

    private final UserJpaRepository userJpaRepository;

    private final NoticesRepository noticesRepository;

    private final FacilityRepository facilityRepository;

    private final ThemeParkRepository themeParkRepository;

    private final PaymentJpaRepository paymentJpaRepository;

    private final AttractionRepository attractionRepository;

    private final SuggestionsRepository suggestionsRepository;

    private final AccommodationRepository accommodationRepository;

    private final AdditionalServiceRepository additionalServiceRepository;

    @Override
    public void logout() {

    }

    @Override
    public FindJwtResult login(AdminLoginCommand command) {

        var user = userJpaRepository.findUserEntityByEmail(command.email())
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));

        if (hasNotAdminRole(user)) {
            throw new EasyCheckException(AdminMessageType.ADMIN_ACCESS_DENIED);
        }

        if (passwordIncorrect(command, user)) {
            throw new EasyCheckException(AdminMessageType.ADMIN_NOT_FOUND);
        }

        var userDetails = new EasyCheckUserDetails(user);

        var accessToken = jwtUtil.createAccessToken(userDetails);
        var refreshToken = jwtUtil.createAccessToken(userDetails);

        return FindJwtResult.findByTokenString(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public FindUserResult updateUserStatus(UserStatusUpdateCommand command) {

        UserEntity userEntity = userJpaRepository.findById(command.userId())
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));

        userEntity.setUserStatus(command.status());

        return FindUserResult.findByUserEntity(userEntity);
    }

    @Override
    public List<FindUserResult> getAllUsers(UserFindQuery query) {
        return List.of();
    }

    @Override
    public FindUserResult getUserDetails(UserFindQuery query) {
        return null;
    }

    @Override
    public FindAdminResult getAdminDetails() {
        return null;
    }

    @Override
    public FindAccommodationResult getManagerAccommodation() {
        Long managerAccommodationId = getManagerAccommodationId();

        if (managerAccommodationId == null) {
            return FindAccommodationResult.findByAccommodationEntityWithName(
                    AccommodationEntity.builder()
                            .name("최종 관리자")
                            .build()

            );
        }

        AccommodationEntity result = accommodationRepository.findById(managerAccommodationId)
                .orElseThrow(() -> new EasyCheckException(AdminMessageType.ACCOMMODATION_NOT_MANAGED));

        return FindAccommodationResult.findByAccommodationEntityWithName(result);
    }

    @Override
    public List<FindSuggestionResult> getAllSuggestions() {
        log.info("[AdminService - getAllSuggestions]");
        return suggestionsRepository.findAllByAccommodationEntity_Id(getManagerAccommodationId())
                .stream()
                .map(FindSuggestionResult::findBySuggestionEntity)
                .toList();
    }

    @Override
    public List<FindFacilitiesResult> getAllFacilities() {
        return facilityRepository.findAllByAccommodationEntity_Id(getManagerAccommodationId())
                .stream()
                .map(FindFacilitiesResult::findByFacilityEntity)
                .toList();
    }

    @Override
    public List<FindAdditionalServiceResult> getAllAdditionalServices() {
        return additionalServiceRepository.findAllByAccommodationEntity_Id(getManagerAccommodationId())
                .stream()
                .map(FindAdditionalServiceResult::findByAdditionalServiceEntity)
                .toList();
    }

    @Override
    public List<FindNoticeResult> getAllNotices() {
        return noticesRepository.findAllByAccommodationEntity_Id(getManagerAccommodationId())
                .stream()
                .map(FindNoticeResult::findByNoticeWithUserAndAccommodation)
                .toList();
    }

    @Override
    public List<FindThemeParkResult> getAllThemeParks() {
        return themeParkRepository.findAllByAccommodation_Id(getManagerAccommodationId())
                .stream()
                .map(FindThemeParkResult::findByThemeParkEntity)
                .toList();
    }

    @Override
    public List<FindEventResult> getAllEvents() {
        return eventRepository.findAllByAccommodationEntity_Id(getManagerAccommodationId())
                .stream()
                .map(FindEventResult::findByEvent)
                .toList();
    }

    @Override
    public List<FindAttractionResult> getAllAttractions() {
        return attractionRepository
                .findAllByAccommodationId(getManagerAccommodationId())
                .stream()
                .map(FindAttractionResult::findByAttractionEntity)
                .toList();
    }

    @Override
    public List<FindPaymentResult> getAllPayments(PaymentFindQuery query) {
        return paymentJpaRepository.findAllPayments(getManagerAccommodationId(), query)
                .stream()
                .map(FindPaymentResult::findByPaymentEntity)
                .toList();
    }

    /**
     * role 테이블에서 [accommodationId]_ADMIN 이런 방식으로 지점 관리자를 구분하고 있습니다.
     * SUPER_ADMIN의 경우 모든 숙박시설에 대한 권한을 가지므로 null을 반환합니다.
     * 최초 로그인 시점에 JWT 토큰에 해당 역할을 같이 포함했고 아래와 같이 Authentication에서 role을 가져와
     * accommodationId를 추출합니다.
     * @return accommodationId 또는 SUPER_ADMIN인 경우 null
     */
    private Long getManagerAccommodationId() {
        // 1. 인증 정보 확인
        Authentication authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new EasyCheckException(AdminMessageType.ACCOMMODATION_ADMIN_AUTHORITY_NOT_FOUND));

        // 2. SUPER_ADMIN 체크
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return null;
        }

        // 3. ROLE_숫자_ADMIN 형식의 권한 찾기
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.matches("ROLE_\\d+_ADMIN"))
                .findFirst()
                .map(role -> role.replace("ROLE_", "").replace("_ADMIN", ""))
                .map(Long::parseLong)
                .orElseThrow(() -> new EasyCheckException(AdminMessageType.ACCOMMODATION_ADMIN_AUTHORITY_NOT_FOUND));
    }

    private boolean hasNotAdminRole(UserEntity user) {
        return !user.getRole().getName().endsWith(UserRole.ADMIN.name());
    }

    private boolean passwordIncorrect(AdminLoginCommand command, UserEntity user) {
        return !passwordEncoder.matches(command.password(), user.getPassword());
    }

}

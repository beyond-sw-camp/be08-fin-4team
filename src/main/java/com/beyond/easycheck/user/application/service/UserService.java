package com.beyond.easycheck.user.application.service;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.common.security.infrastructure.persistence.entity.ExpiredAccessToken;
import com.beyond.easycheck.common.security.infrastructure.persistence.repository.ExpiredAccessTokenJpaRepository;
import com.beyond.easycheck.common.security.utils.JwtUtil;
import com.beyond.easycheck.corporate.application.CorporateOperationUseCase;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.repository.VerifiedEmailRepository;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.repository.SmsVerifiedPhoneRepository;
import com.beyond.easycheck.user.application.domain.EasyCheckUserDetails;
import com.beyond.easycheck.user.application.domain.UserRole;
import com.beyond.easycheck.user.application.domain.UserStatus;
import com.beyond.easycheck.user.exception.UserMessageType;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.role.RoleEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.RoleJpaRepository;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserOperationUseCase, UserReadUseCase {

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    private final RoleJpaRepository roleJpaRepository;

    private final UserJpaRepository userJpaRepository;

    private final VerifiedEmailRepository verifiedEmailRepository;

    private final CorporateOperationUseCase corporateOperationUseCase;

    private final SmsVerifiedPhoneRepository smsVerifiedPhoneRepository;

    private final ExpiredAccessTokenJpaRepository expiredAccessTokenJpaRepository;

    @Override
    @Transactional
    public FindUserResult registerUser(UserRegisterCommand command) {
        log.info("[registerUser] - command = {}", command);

        checkEmailIsDuplicated(command.email());
        // 이메일 인증을 했는지 확인
//        checkEmailIsVerified(command.email());

        UserEntity user = UserEntity.createUser(command);
        log.info("[registerUser] - userEntity after createUser = {}", user);

        // 휴대폰 인증을 했는지 확인
        checkPhoneIsVerified(command.phone());

        // 회원 저장하기 전에 비밀번호 암호화
        String securePassword = passwordEncoder.encode(command.password());
        user.setSecurePassword(securePassword);

        // 회원의 역할 설정
        RoleEntity role = findRoleByName(UserRole.USER.name());
        user.setRole(role);
        user.setUserStatus(UserStatus.ACTIVE);

        // 회원 저장
        UserEntity result = userJpaRepository.save(user);
        log.info("[registerUser] - userEntity save result = {}", result);

        return FindUserResult.findByUserEntity(result);
    }

    @Override
    @Transactional
    public FindUserResult registerCorporateUser(CorporateUserRegisterCommand command) {

        // 핸드폰 인증여부 확인
        checkPhoneIsVerified(command.phone());

        UserEntity user = UserEntity.createCorporateUser(command);

        userJpaRepository.save(user);

//        corporateOperationUseCase.createCorporate(corporateCreateCommand);

        return FindUserResult.findByUserEntity(user);
    }

    @Override
    public FindUserResult getUserInfo(UserFindQuery query) {
        log.info("[getUserInfo] - query = {}", query);

        UserEntity user = findUserById(query.userId());

        log.info("[getUserInfo] - user = {}", user);
        return FindUserResult.findByUserEntity(user);
    }

    @Override
    public void checkEmailDuplicated(UserFindQuery query) {
        checkEmailIsDuplicated(query.email());
    }

    @Override
    public FindJwtResult login(UserLoginCommand command) {

        log.info("[login] - login command = {}", command);
        UserEntity user = findUserByEmail(command.email());

        log.info("[login] - user find result = {}", user);
        if (passwordIncorrect(command, user)) {
            throw new EasyCheckException(UserMessageType.USER_NOT_FOUND);
        }

        EasyCheckUserDetails userDetails = new EasyCheckUserDetails(user);
        log.info("[login] - user details = {}", userDetails);

        return generateJwt(userDetails);
    }

    @Override
    public FindJwtResult loginGuest(GuestUserLoginCommand command) {

        log.info("[loginGuest] - login command = {}", command);
        UserEntity guestUserEntity = UserEntity.createGuestUser(command.name(), command.phone());

        RoleEntity roleEntity = findRoleByName(UserRole.GUEST.name());
        guestUserEntity.setRole(roleEntity);

        EasyCheckUserDetails userDetails = new EasyCheckUserDetails(guestUserEntity);
        log.info("[loginGuest] - guestUser details = {}", userDetails);

        return generateJwt(userDetails);
    }

    @Override
    @Transactional
    public void logout(UserLogoutCommand command) {

        // 현재 로그아웃 하는 accessToken 만료 토큰으로 등록
        ExpiredAccessToken expiredAccessToken = ExpiredAccessToken.createExpiredAccessToken(
                command.accessToken()
        );

        expiredAccessTokenJpaRepository.save(expiredAccessToken);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordCommand command) {

        // 비밀번호 변경 전 이메일 인증 과정을 거쳐야 한다.
        // checkEmailIsVerified(command.email());

        UserEntity user = findUserByEmail(command.email());

        // 기존 패스워드와 일치하는지 검사
        if (!passwordEncoder.matches(command.oldPassword(), user.getPassword())) {
            throw new EasyCheckException(UserMessageType.PASSWORD_INCORRECT);
        }

        String newSecurePassword = passwordEncoder.encode(command.newPassword());
        user.setSecurePassword(newSecurePassword);

    }

    @Override
    @Transactional
    public FindUserResult updateUserInfo(UserUpdateCommand command) {
        checkPhoneIsVerified(command.phone());

        UserEntity user = findUserById(command.userId());

        user.updateUser(command);

        return FindUserResult.findByUserEntity(user);
    }

    @Override
    @Transactional
    public void deactivateUser(DeactivateUserCommand command) {
        UserEntity user = findUserById(command.userId());

        user.setUserStatus(UserStatus.DEACTIVATED);
    }

    private FindJwtResult generateJwt(EasyCheckUserDetails userDetails) {
        return FindJwtResult.findByTokenString(
                jwtUtil.createAccessToken(userDetails),
                jwtUtil.createRefreshToken(userDetails)
        );
    }

    private boolean passwordIncorrect(UserLoginCommand command, UserEntity user) {
        return !passwordEncoder.matches(command.password(), user.getPassword());
    }

    private void checkEmailIsVerified(String email) {
        verifiedEmailRepository.findById(email)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.EMAIL_NOT_VERIFIED));
    }

    private void checkPhoneIsVerified(String phone) {
        smsVerifiedPhoneRepository.findById(phone)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.PHONE_NOT_VERIFIED));
    }

    private void checkEmailIsDuplicated(String email) {
        userJpaRepository.findUserEntityByEmail(email)
                .ifPresent(userEntity -> {
                    throw new EasyCheckException(UserMessageType.USER_ALREADY_EXISTS);
                });
    }

    private UserEntity findUserById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));
    }

    private UserEntity findUserByEmail(String email) {
        return userJpaRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_NOT_FOUND));
    }

    private RoleEntity findRoleByName(String name) {
        return roleJpaRepository.findRoleEntityByName(name)
                .orElseThrow(() -> new EasyCheckException(UserMessageType.USER_ROLE_INVALID));
    }
}

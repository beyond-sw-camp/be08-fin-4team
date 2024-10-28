package com.beyond.easycheck.user.application.service.user;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.common.security.utils.JwtUtil;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.entity.VerifiedEmailEntity;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.repository.VerifiedEmailRepository;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.entity.VerifiedPhone;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.repository.SmsVerifiedPhoneRepository;
import com.beyond.easycheck.user.application.domain.EasyCheckUserDetails;
import com.beyond.easycheck.user.application.domain.UserRole;
import com.beyond.easycheck.user.application.domain.UserStatus;
import com.beyond.easycheck.user.application.service.UserService;
import com.beyond.easycheck.user.exception.UserMessageType;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.role.RoleEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.RoleJpaRepository;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.beyond.easycheck.user.application.service.UserOperationUseCase.*;
import static com.beyond.easycheck.user.application.service.UserReadUseCase.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RoleJpaRepository roleJpaRepository;

    @Mock
    UserJpaRepository userJpaRepository;

    @Mock
    VerifiedEmailRepository verifiedEmailRepository;

    @Mock
    SmsVerifiedPhoneRepository smsVerifiedPhoneRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("[회원가입] - 성공")
    void registerUser_success() {
        // given
        UserRegisterCommand command = new UserRegisterCommand(
                "existing@gmail.com", "password", "Test User", "010-1234-5678",
                "서울시", "강남구", 'Y'
        );

        // 이메일 인증 했다고 가정
        VerifiedEmailEntity verifiedEmailMock = mock(VerifiedEmailEntity.class);
        // 핸드폰 인증 했다고 가정
        VerifiedPhone verifiedPhoneMock = mock(VerifiedPhone.class);
        RoleEntity roleEntityMock = mock(RoleEntity.class);

        UserEntity userEntity = UserEntity.createUser(command);
        userEntity.setUserStatus(UserStatus.ACTIVE);
        userEntity.setRole(roleEntityMock);

        when(verifiedEmailRepository.findById(command.email())).thenReturn(Optional.of(verifiedEmailMock));
        when(smsVerifiedPhoneRepository.findById(command.phone())).thenReturn(Optional.of(verifiedPhoneMock));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(roleJpaRepository.findRoleEntityByName(UserRole.USER.name())).thenReturn(Optional.of(roleEntityMock));
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(userEntity); // 변경된 부분

        // when
        FindUserResult result = userService.registerUser(command);

        // then
        verify(userJpaRepository).save(any(UserEntity.class));
        assertEquals(command.addr(), result.addr());
        assertEquals(command.email(), result.email());
    }

    @Test
    @DisplayName("[회원가입] - 실패 - 중복된 이메일")
    void registerUser_fail_duplicatedEmail() {
        // given
        UserRegisterCommand command = new UserRegisterCommand(
                "existing@gmail.com", "password", "Test User", "010-1234-5678",
                "서울시", "강남구", 'Y'
        );

        when(userJpaRepository.findUserEntityByEmail(command.email())).thenReturn(Optional.of(mock(UserEntity.class)));

        // when & then
        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(UserMessageType.USER_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("[회원가입] - 실패 - 인증되지 않은 이메일")
    void registerUser_fail_unauthorizedEmail() {
        // given
        UserRegisterCommand command = new UserRegisterCommand(
                "existing@gmail.com", "password", "Test User", "010-1234-5678",
                "서울시", "강남구", 'Y'
        );

        // when & then
        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(UserMessageType.EMAIL_NOT_VERIFIED.getMessage());
    }

    @Test
    @DisplayName("[회원가입] - 실패 - 인증되지 않은 이메일")
    void registerUser_fail_unauthorizedPhone() {
        // given
        UserRegisterCommand command = new UserRegisterCommand(
                "existing@gmail.com", "password", "Test User", "010-1234-5678",
                "서울시", "강남구", 'Y'
        );
        VerifiedEmailEntity verifiedEmailMock = mock(VerifiedEmailEntity.class);

        when(verifiedEmailRepository.findById(command.email())).thenReturn(Optional.of(verifiedEmailMock));

        // when & then
        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(EasyCheckException.class)
                .hasMessage(UserMessageType.PHONE_NOT_VERIFIED.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("[로그인] - 성공")
    void login_success() {
        // given
        UserLoginCommand command = new UserLoginCommand("test@gmail.com", "password");

        UserEntity userEntity = new UserEntity();
        userEntity.setUserStatus(mock(UserStatus.class));
        userEntity.setRole(mock(RoleEntity.class));

        EasyCheckUserDetails userDetailsMock = mock(EasyCheckUserDetails.class);

        when(userJpaRepository.findUserEntityByEmail(command.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(command.password(), userEntity.getPassword())).thenReturn(true);
        when(jwtUtil.createAccessToken(any())).thenReturn("accessToken");
        when(jwtUtil.createRefreshToken(any())).thenReturn("refreshToken");
        // when
        FindJwtResult result = userService.login(command);
        // then
        assertThat(result.accessToken()).isEqualTo("accessToken");
        assertThat(result.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("[로그인] - 실패 - 유저를 찾을 수 없음 - 이메일")
    void login_failedWithNotFound_email() {
        // given
        UserLoginCommand command = new UserLoginCommand("test@gmail.com", "password");

        when(userJpaRepository.findUserEntityByEmail(command.email())).thenReturn(Optional.empty());

        // when & then
        EasyCheckException exception = assertThrows(EasyCheckException.class, () -> userService.login(command));
        assertEquals(UserMessageType.USER_NOT_FOUND.name(), exception.getType());

        verify(userJpaRepository).findUserEntityByEmail(command.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("[로그인] - 실패 - 유저를 찾을 수 없음 - 비밀번호")
    void login_failedWithNotFound_password() {
        // given
        UserLoginCommand command = new UserLoginCommand("test@gmail.com", "password");
        UserEntity userEntity = new UserEntity(); // Assume proper initialization

        when(userJpaRepository.findUserEntityByEmail(command.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(command.password(), userEntity.getPassword())).thenReturn(false);

        // when & then
        EasyCheckException exception = assertThrows(EasyCheckException.class, () -> userService.login(command));
        assertEquals(UserMessageType.USER_NOT_FOUND.name(), exception.getType());

        verify(userJpaRepository).findUserEntityByEmail(command.email());
        verify(passwordEncoder).matches(command.password(), userEntity.getPassword());

    }

    @Test
    @DisplayName("[비밀번호 변경] - 성공")
    void changePassword() {
        // Given
        String email = "test@example.com";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        ChangePasswordCommand command = new ChangePasswordCommand(email, oldPassword, newPassword);

        UserEntity user = new UserEntity(); // Assume appropriate constructor
        user.setSecurePassword(passwordEncoder.encode(oldPassword));

        when(verifiedEmailRepository.findById(email)).thenReturn(Optional.of(mock(VerifiedEmailEntity.class)));
        when(userJpaRepository.findUserEntityByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // When
        userService.changePassword(command);

        // Then
        verify(verifiedEmailRepository).findById(email);
        verify(userJpaRepository).findUserEntityByEmail(email);
        verify(passwordEncoder).matches(oldPassword, passwordEncoder.encode(oldPassword));
        verify(passwordEncoder).encode(newPassword);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    @DisplayName("[비밀번호 변경] - 실패 - 이메일 미인증")
    void changePassword_EmailNotVerified() {
        // Given
        String email = "test@example.com";
        ChangePasswordCommand command = new ChangePasswordCommand(email, "oldPassword", "newPassword");

        when(verifiedEmailRepository.findById(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EasyCheckException.class, () -> userService.changePassword(command));
        verify(verifiedEmailRepository).findById(email);
        verifyNoInteractions(userJpaRepository, passwordEncoder);
    }

    @Test
    @DisplayName("[비밀번호 변경] - 실패 - 비밀번호 불일치")
    void changePassword_OldPasswordIncorrect() {
        // Given
        String email = "test@example.com";
        ChangePasswordCommand command = new ChangePasswordCommand(email, "oldPassword", "newPassword");

        UserEntity mockUserEntity = mock(UserEntity.class);
        mockUserEntity.setSecurePassword("qwer1234");

        when(verifiedEmailRepository.findById(email)).thenReturn(Optional.of(mock(VerifiedEmailEntity.class)));
        when(userJpaRepository.findUserEntityByEmail(email)).thenReturn(Optional.of(mock(UserEntity.class)));

        // When & Then
        assertThrows(EasyCheckException.class, () -> userService.changePassword(command));
        verify(verifiedEmailRepository).findById(email);
    }

    @Test
    @DisplayName("[유저 정보 불러오기] - 성공")
    void getUserInfo_success() {
        // Given
        Long userId = 1L;
        UserFindQuery query = UserFindQuery.builder()
                .userId(userId)
                .build();

        RoleEntity roleEntity = new RoleEntity() {
            @Override
            public String getName() {
                return "USER";
            }
        };

        UserRegisterCommand command = new UserRegisterCommand(
                "existing@gmail.com", "password", "Test User", "010-1234-5678",
                "서울시", "강남구", 'Y'
        );

        UserEntity user = UserEntity.createUser(command);
        user.setRole(roleEntity);  // Role 설정

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        FindUserResult result = userService.getUserInfo(query);

        // Then
        assertNotNull(result);
        assertEquals("existing@gmail.com", result.email());
        assertEquals("Test User", result.name());
        assertEquals("010-1234-5678", result.phone());
        assertEquals(UserStatus.ACTIVE.name(), result.status());
        assertEquals("USER", result.role());

        verify(userJpaRepository).findById(userId);
    }

    @Test
    @DisplayName("[유저 정보 수정] - 성공")
    void updateUserInfo_success() {
        // Given
        Long userId = 1L;
        UserUpdateCommand command = new UserUpdateCommand(
                userId,
                "Updated User",
                "010-9999-8888",
                "Updated City",
                "Updated District"
        );

        UserEntity user = mock(UserEntity.class);
        RoleEntity role = mock(RoleEntity.class);
        VerifiedPhone verifiedPhone = mock(VerifiedPhone.class);

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));
        when(smsVerifiedPhoneRepository.findById(command.phone())).thenReturn(Optional.of(verifiedPhone));
        when(user.getName()).thenReturn("Updated User");
        when(user.getPhone()).thenReturn("010-9999-8888");
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(user.getRole()).thenReturn(role);

        // When
        FindUserResult result = userService.updateUserInfo(command);

        // Then
        verify(user).updateUser(command);
        verify(smsVerifiedPhoneRepository).findById(command.phone());
        assertEquals("Updated User", result.name());
        assertEquals("010-9999-8888", result.phone());
    }

    @Test
    @DisplayName("[유저 정보 수정] - 실패 - 전화번호 미인증")
    void updateUserInfo_fail_phoneNotVerified() {
        // Given
        Long userId = 1L;
        UserUpdateCommand command = new UserUpdateCommand(
                userId,
                "Updated User",
                "010-9999-8888",
                "Updated City",
                "Updated District"
        );

        when(smsVerifiedPhoneRepository.findById(command.phone())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EasyCheckException.class, () -> userService.updateUserInfo(command));
        verify(smsVerifiedPhoneRepository).findById(command.phone());
        verify(userJpaRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("[유저 정보 수정] - 실패 - 사용자를 찾을 수 없음")
    void updateUserInfo_fail_userNotFound() {
        // Given
        Long userId = 1L;
        UserUpdateCommand command = new UserUpdateCommand(
                userId,
                "Updated User",
                "010-9999-8888",
                "Updated City",
                "Updated District"
        );

        VerifiedPhone verifiedPhone = mock(VerifiedPhone.class);
        when(smsVerifiedPhoneRepository.findById(command.phone())).thenReturn(Optional.of(verifiedPhone));
        when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EasyCheckException.class, () -> userService.updateUserInfo(command));
        verify(smsVerifiedPhoneRepository).findById(command.phone());
        verify(userJpaRepository).findById(userId);
    }

    @Test
    @DisplayName("[회원 비활성화] - 성공")
    void deactivateUser_success() {
        // Given
        Long userId = 1L;
        DeactivateUserCommand command = new DeactivateUserCommand(userId);
        UserEntity user = mock(UserEntity.class);

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deactivateUser(command);

        // Then
        verify(userJpaRepository).findById(userId);
        verify(user).setUserStatus(UserStatus.DEACTIVATED);
    }

    @Test
    @DisplayName("[회원 비활성화] - 실패 - 사용자를 찾을 수 없음")
    void deactivateUser_fail_userNotFound() {
        // Given
        Long userId = 1L;
        DeactivateUserCommand command = new DeactivateUserCommand(userId);

        when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EasyCheckException.class, () -> userService.deactivateUser(command));
        verify(userJpaRepository).findById(userId);
    }



}

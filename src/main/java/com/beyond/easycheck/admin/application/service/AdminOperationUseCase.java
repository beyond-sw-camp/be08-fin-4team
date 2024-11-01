package com.beyond.easycheck.admin.application.service;

import com.beyond.easycheck.user.application.domain.UserStatus;
import com.beyond.easycheck.user.application.service.UserReadUseCase;
import com.beyond.easycheck.user.application.service.UserReadUseCase.FindUserResult;

import static com.beyond.easycheck.admin.application.service.AdminReadUseCase.*;

public interface AdminOperationUseCase {

    void logout();

    FindJwtResult login(AdminLoginCommand command);

    FindUserResult updateUserStatus(UserStatusUpdateCommand command);

    record AdminLoginCommand(
            String email,
            String password
    ) {
    }

    record UserStatusUpdateCommand(
            Long userId,
            UserStatus status
    ) {
    }

    record FindJwtResult(
            String accessToken,
            String refreshToken
    ) {
        public static FindJwtResult findByTokenString(String accessToken, String refreshToken) {
            return new FindJwtResult(accessToken, refreshToken);
        }
    }

}

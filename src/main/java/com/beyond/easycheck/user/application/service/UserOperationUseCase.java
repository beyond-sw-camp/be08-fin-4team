package com.beyond.easycheck.user.application.service;

import com.beyond.easycheck.corporate.ui.requestbody.CorporateCreateRequest;
import com.beyond.easycheck.user.application.service.UserReadUseCase.FindJwtResult;
import com.beyond.easycheck.user.application.service.UserReadUseCase.FindUserResult;
import org.springframework.web.multipart.MultipartFile;

public interface UserOperationUseCase {

    void logout(UserLogoutCommand command);

    FindJwtResult login(UserLoginCommand command);

    void deactivateUser(DeactivateUserCommand command);

    void changePassword(ChangePasswordCommand command);

    FindJwtResult loginGuest(GuestUserLoginCommand command);

    FindUserResult registerUser(UserRegisterCommand command);

    FindUserResult updateUserInfo(UserUpdateCommand command);

    FindUserResult registerCorporateUser(CorporateUserRegisterCommand command);

    record CorporateUserRegisterCommand(
            String name,
            String phone,
            String email
    ) {

    }

    record UserRegisterCommand(
            String email,
            String password,
            String name,
            String phone,
            String addr,
            String addrDetail,
            char marketingConsent
    ) {
    }

    record UserUpdateCommand(
            Long userId,
            String email,
            String phone,
            String addr,
            String addrDetail
    ) {
    }

    record UserLoginCommand(
            String email,
            String password
    ) {
    }

    record GuestUserLoginCommand(
            String name,
            String phone
    ) {
    }

    record UserLogoutCommand(
            String accessToken,
            Long userId
    ) {
    }

    record ChangePasswordCommand(
            String email,
            String oldPassword,
            String newPassword
    ) {
    }

    record DeactivateUserCommand(Long userId) {

    }
}

package com.beyond.easycheck.user.exception;

import com.beyond.easycheck.common.exception.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserMessageType implements MessageType {

    USER_NOT_FOUND("User not found in the system", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("User with this identifier already exists in the system", HttpStatus.CONFLICT),
    USER_ROLE_INVALID("Invalid or non-existent user role specified", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT("", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATE("The new password cannot be the same as the previous password", HttpStatus.BAD_REQUEST),


    USER_UNAUTHORIZED("Authentication required to access this resource", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED("Email address has not been verified", HttpStatus.UNAUTHORIZED),
    PHONE_NOT_VERIFIED("Phone number has not been verified", HttpStatus.UNAUTHORIZED),
    ;

    private final String message;
    private final HttpStatus status;
}

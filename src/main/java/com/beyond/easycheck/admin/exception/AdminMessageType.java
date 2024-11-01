package com.beyond.easycheck.admin.exception;

import com.beyond.easycheck.common.exception.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminMessageType implements MessageType {

    // 권한 관련
    ACCOMMODATION_ADMIN_AUTHORITY_NOT_FOUND("숙소 관리자 권한이 존재하지 않습니다.", HttpStatus.FORBIDDEN),
    ADMIN_ACCESS_DENIED("관리자 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_ADMIN_ROLE("유효하지 않은 관리자 권한입니다.", HttpStatus.FORBIDDEN),
    ADMIN_PERMISSION_REQUIRED("해당 작업을 수행할 관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN),

    // 인증 관련
    ADMIN_NOT_FOUND("존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND),
    ADMIN_LOGIN_FAILED("관리자 로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    ADMIN_TOKEN_EXPIRED("관리자 인증이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    INVALID_ADMIN_TOKEN("유효하지 않은 관리자 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // 숙소 관련
    ACCOMMODATION_NOT_MANAGED("관리 대상이 아닌 숙소입니다.", HttpStatus.FORBIDDEN),
    ACCOMMODATION_MANAGEMENT_LIMIT_EXCEEDED("관리 가능한 숙소 수를 초과했습니다.", HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatus status;
}
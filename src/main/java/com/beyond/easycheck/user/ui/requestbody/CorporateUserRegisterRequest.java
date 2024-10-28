package com.beyond.easycheck.user.ui.requestbody;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record CorporateUserRegisterRequest(
        @NotEmpty
        String name,
        @NotEmpty
        String phone,
        @Email
        String email
        // 휴대폰 본인인증 동의 여부 필요하면 추가
) {
}

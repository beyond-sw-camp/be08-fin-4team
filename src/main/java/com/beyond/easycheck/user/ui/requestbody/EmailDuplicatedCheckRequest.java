package com.beyond.easycheck.user.ui.requestbody;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record EmailDuplicatedCheckRequest(
        @NotEmpty
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
) {
}

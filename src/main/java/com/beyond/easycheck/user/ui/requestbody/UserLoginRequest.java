package com.beyond.easycheck.user.ui.requestbody;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserLoginRequest(
        @NotEmpty
        @Email
        String email,
        @NotEmpty
        String password
) {
}

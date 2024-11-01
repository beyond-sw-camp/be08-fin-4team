package com.beyond.easycheck.admin.ui.requestbody;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}

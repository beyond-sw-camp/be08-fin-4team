package com.beyond.easycheck.user.ui.requestbody;

import jakarta.validation.constraints.NotBlank;

public record FindPasswordRequest(
        @NotBlank
        String email,
        @NotBlank
        String phone,
        @NotBlank
        String newPassword,
        @NotBlank
        String confirmPassword
) {
}
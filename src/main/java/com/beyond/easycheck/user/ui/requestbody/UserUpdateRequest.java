package com.beyond.easycheck.user.ui.requestbody;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String phone,
        @NotBlank
        String addr,
        @NotBlank
        String addrDetail
) {
}

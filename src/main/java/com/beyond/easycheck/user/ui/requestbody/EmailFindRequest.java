package com.beyond.easycheck.user.ui.requestbody;

import jakarta.validation.constraints.NotBlank;

public record EmailFindRequest(
        @NotBlank
        String name,
        @NotBlank
        String phone
) {
}

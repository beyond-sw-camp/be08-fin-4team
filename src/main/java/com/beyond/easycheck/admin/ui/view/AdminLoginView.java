package com.beyond.easycheck.admin.ui.view;

import com.beyond.easycheck.admin.application.service.AdminOperationUseCase;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import static com.beyond.easycheck.admin.application.service.AdminOperationUseCase.*;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminLoginView {

    private final String accessToken;
    private final String refreshToken;

    public AdminLoginView(FindJwtResult result) {
        this.accessToken = result.accessToken();
        this.refreshToken = result.refreshToken();
    }
}

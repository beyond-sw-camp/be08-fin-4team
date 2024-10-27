package com.beyond.easycheck.user.ui.controller;


import com.beyond.easycheck.user.application.service.UserOperationUseCase;
import com.beyond.easycheck.user.ui.requestbody.CorporateUserRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.beyond.easycheck.user.application.service.UserOperationUseCase.CorporateUserRegisterCommand;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/corp-users")
@Tag(name = "CorporateUser", description = "법인회원 관련 API")
public class CorporateUserController {

    private final UserOperationUseCase userOperationUseCase;

    @PostMapping("")
    @Operation(summary = "법인회원 회원가입")
    public ResponseEntity<Void> register(@RequestBody @Validated CorporateUserRegisterRequest request) {

        CorporateUserRegisterCommand command = new CorporateUserRegisterCommand(request.name(), request.email(), request.email());

        userOperationUseCase.registerCorporateUser(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
}

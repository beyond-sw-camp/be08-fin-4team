package com.beyond.easycheck.common.ui.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Common", description = "어플리케이션 상태 확인용")
public class CommonController {

    @GetMapping("/health-check")
    @Operation(
            summary = "통신 상태 확인",
            description = "EasyCheck 에플리케이션 통신 상태 확인용"
    )
    public ResponseEntity<String> healthCheck() {
        log.info("[health-check] 요청 수신");
        return ResponseEntity.ok("ok");
    }

}

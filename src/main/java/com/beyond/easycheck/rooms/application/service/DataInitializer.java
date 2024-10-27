package com.beyond.easycheck.rooms.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomService roomService;

    @Override
    public void run(String... args) {
        roomService.initializeInitialRoomAvailability();
        System.out.println("초기 객실 가용성 4개월간 설정 완료");
    }
}

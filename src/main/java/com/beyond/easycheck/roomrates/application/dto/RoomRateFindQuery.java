package com.beyond.easycheck.roomrates.application.dto;

import java.time.LocalDate;

public record RoomRateFindQuery(
        Long roomId,
        LocalDate seasonStartDate
) {

}

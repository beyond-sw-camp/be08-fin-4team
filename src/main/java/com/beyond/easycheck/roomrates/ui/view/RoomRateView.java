package com.beyond.easycheck.roomrates.ui.view;

import com.beyond.easycheck.roomrates.infrastructure.entity.RoomrateType;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomRateView {

    private Long id;

    private BigDecimal normalRate;

    private BigDecimal corpRate;

    private String seasonName;

    public RoomRateView(Long id, BigDecimal normalRate, BigDecimal corpRate, String seasonName) {
        this.id = id;
        this.normalRate = normalRate;
        this.corpRate = corpRate;
        this.seasonName = seasonName;
    }
}

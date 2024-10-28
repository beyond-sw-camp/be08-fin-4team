package com.beyond.easycheck.roomrates.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.roomrates.ui.requestbody.RoomrateUpdateRequest;
import com.beyond.easycheck.rooms.infrastructure.entity.RoomEntity;
import com.beyond.easycheck.seasons.infrastructure.entity.SeasonEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Builder
@Table(name = "room_rate")
@NoArgsConstructor
@AllArgsConstructor
public class RoomRateEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @JsonManagedReference
    private RoomEntity roomEntity;

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    @JsonManagedReference
    private SeasonEntity seasonEntity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomrateType rateType;

    private String userType;

    @Column(nullable = false)
    private BigDecimal rate;

    public void update(RoomrateUpdateRequest roomrateUpdateRequest, RoomEntity newRoomEntity, SeasonEntity newSeasonEntity) {
        this.roomEntity = newRoomEntity;
        this.seasonEntity = newSeasonEntity;
        this.rateType = roomrateUpdateRequest.getRateType();
        this.rate = roomrateUpdateRequest.getRate();
    }

}

package com.beyond.easycheck.seasons.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.seasons.application.domain.DayType;
import com.beyond.easycheck.seasons.ui.requestbody.SeasonUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@Builder
@Table(name = "season")
@NoArgsConstructor
@AllArgsConstructor
public class SeasonEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seasonName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private DayType dayType;

    public void update(SeasonUpdateRequest seasonUpdateRequest) {
        seasonName = seasonUpdateRequest.getSeasonName();
        description = seasonUpdateRequest.getDescription();
        startDate = seasonUpdateRequest.getStartDate();
        endDate = seasonUpdateRequest.getEndDate();
    }

}

package com.beyond.easycheck.roomtypes.infrastructure.entity;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.roomtypes.ui.requestbody.RoomtypeUpdateRequest;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Entity
@Builder
@Table(name = "room_type")
@NoArgsConstructor
@AllArgsConstructor
public class RoomtypeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTypeId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    @JsonManagedReference
    private AccommodationEntity accommodationEntity;

    @Column(nullable = false)
    private String name;

    public void update(RoomtypeUpdateRequest roomTypeUpdateRequest) {
        name = roomTypeUpdateRequest.getTypeName();
    }

}

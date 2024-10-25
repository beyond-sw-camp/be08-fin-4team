package com.beyond.easycheck.rooms.infrastructure.entity;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.rooms.ui.requestbody.RoomUpdateRequest;
import com.beyond.easycheck.roomtypes.infrastructure.entity.RoomtypeEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static com.beyond.easycheck.rooms.exception.RoomMessageType.ARGUMENT_NOT_VALID;

@Setter
@Getter
@Entity
@Builder
@Table(name = "room")
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    @JsonManagedReference
    private RoomtypeEntity roomTypeEntity;

    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageEntity> images = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(nullable = false)
    private int roomAmount;

    @Column(nullable = false)
    private int remainingRoom;

    @PrePersist
    public void prePersist() {
        this.remainingRoom = this.roomAmount;
    }

    public void update(RoomUpdateRequest roomUpdateRequest) {
        type = roomUpdateRequest.getRoomNumber();
        roomAmount = roomUpdateRequest.getRoomAmount();
        status = roomUpdateRequest.getStatus();

        if (roomUpdateRequest.getRoomAmount() <= 0) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }
    }

    public void addImage(ImageEntity imageEntity) {
        this.images.add(imageEntity);
        imageEntity.setRoom(this);
    }

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "room_image")
    public static class ImageEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "image_id")
        private Long id;

        @Column(nullable = false)
        private String url;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "room_id", nullable = false)
        private RoomEntity room;

        public static ImageEntity createImage(String url, RoomEntity room) {
            return new ImageEntity(null, url, room);
        }

    }

}

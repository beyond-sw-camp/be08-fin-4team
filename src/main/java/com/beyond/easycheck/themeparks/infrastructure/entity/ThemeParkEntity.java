package com.beyond.easycheck.themeparks.infrastructure.entity;


import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.themeparks.application.service.ThemeParkOperationUseCase.ThemeParkCreateCommand;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "theme_park")
public class ThemeParkEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_park_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String guidePageName;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private AccommodationEntity accommodation;

    private String ticketAvailable;

    @OneToMany(mappedBy = "themePark", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageEntity> images = new ArrayList<>();


    public static ThemeParkEntity createThemePark(ThemeParkCreateCommand command, AccommodationEntity accommodation) {
        return new ThemeParkEntity(
                null,
                command.getName(),
                command.getGuidePageName(),
                command.getDescription(),
                accommodation,
                command.getTicketAvailable(),
                new ArrayList<>()
        );
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addImage(ImageEntity imageEntity) {
        this.images.add(imageEntity);
        imageEntity.setThemePark(this);
    }

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "theme_park_image")
    public static class ImageEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "image_id")
        private Long id;

        @Column(nullable = false)
        private String url;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "theme_park_id", nullable = false)
        private ThemeParkEntity themePark;

        public static ImageEntity createImage(String url, ThemeParkEntity themePark) {
            return new ImageEntity(null, url, themePark);
        }
    }
}

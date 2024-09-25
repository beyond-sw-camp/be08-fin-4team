package com.beyond.easycheck.themeparks.application.service;

import com.beyond.easycheck.common.entity.BaseTimeEntity;
import com.beyond.easycheck.themeparks.infrastructure.persistence.entity.ThemeParkEntity;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

public interface ThemeParkReadUseCase {


    @Getter
    @Builder
    class FindThemeParkResult extends BaseTimeEntity {
        private final Long id;

        private final String name;

        private final String description;

        private final String location;

        private final String image;

        public static FindThemeParkResult findByThemeParkEntity(ThemeParkEntity themePark){
            return FindThemeParkResult.builder()
                    .id(themePark.getId())
                    .name(themePark.getName())
                    .description(themePark.getDescription())
                    .location(themePark.getLocation())
                    .image(themePark.getImage())
                    .build();
        }
    }
}
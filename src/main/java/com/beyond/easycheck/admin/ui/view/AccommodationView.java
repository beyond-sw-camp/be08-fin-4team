package com.beyond.easycheck.admin.ui.view;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationType;
import com.beyond.easycheck.admin.application.service.AdminReadUseCase;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.beyond.easycheck.admin.application.service.AdminReadUseCase.*;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccommodationView {
    private final Long id;

    private final String name;

    private final List<String> thumbnailUrls;

    private final List<String> landscapeUrls;

    private final String address;

    private final String directionsUrl;

    private final String latitude;

    private final String longitude;

    private final String responseTime;

    private AccommodationType accommodationType;

    public AccommodationView(FindAccommodationResult result) {
        this.id = result.id();
        this.name = result.name();
        this.thumbnailUrls = result.thumbnailUrls();
        this.landscapeUrls = result.landscapeUrls();
        this.address = result.address();
        this.directionsUrl = result.directionsUrl();
        this.latitude = result.latitude();
        this.longitude = result.longitude();
        this.responseTime = result.responseTime();
        this.accommodationType = result.accommodationType();
    }
}

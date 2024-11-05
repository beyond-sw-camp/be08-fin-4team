package com.beyond.easycheck.events.application.service;

import com.beyond.easycheck.accomodations.infrastructure.entity.AccommodationEntity;
import com.beyond.easycheck.accomodations.infrastructure.repository.AccommodationRepository;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.events.application.service.dto.EventFindQuery;
import com.beyond.easycheck.events.infrastructure.entity.EventEntity;
import com.beyond.easycheck.events.infrastructure.repository.EventImageRepository;
import com.beyond.easycheck.events.infrastructure.repository.EventRepository;
import com.beyond.easycheck.events.ui.requestbody.EventCreateRequest;
import com.beyond.easycheck.events.ui.requestbody.EventUpdateRequest;
import com.beyond.easycheck.events.ui.view.EventView;
import com.beyond.easycheck.s3.application.service.S3Service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.beyond.easycheck.accomodations.exception.AccommodationMessageType.ACCOMMODATION_NOT_FOUND;
import static com.beyond.easycheck.events.exception.EventMessageType.*;
import static com.beyond.easycheck.s3.application.domain.FileManagementCategory.EVENT;
import static com.beyond.easycheck.s3.application.domain.FileManagementCategory.ROOM;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final AccommodationRepository accommodationRepository;
    private final S3Service s3Service;

    private void addImagesToEvent(EventEntity eventEntity, List<String> imageUrls) {
        List<EventEntity.ImageEntity> newImageEntities = imageUrls.stream()
                .map(url -> EventEntity.ImageEntity.createImage(url, eventEntity))
                .toList();

        if (eventEntity.getImages() == null) {
            eventEntity.setImages(new ArrayList<>());
        }

        for (EventEntity.ImageEntity newImage : newImageEntities) {
            if (!eventEntity.getImages().contains(newImage)) {
                eventEntity.addImage(newImage);
            }
        }
    }

    @Transactional
    public EventEntity createEvent(EventCreateRequest eventCreateRequest, List<MultipartFile> imageFiles) {

        AccommodationEntity accommodationEntity = accommodationRepository.findById(eventCreateRequest.getAccommodationEntity())
                .orElseThrow(() -> new EasyCheckException(ACCOMMODATION_NOT_FOUND));

        if (eventCreateRequest.getEventName() == null || eventCreateRequest.getDetail() == null
                || eventCreateRequest.getStartDate() == null || eventCreateRequest.getEndDate() == null) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }

        List<String> imageUrls = s3Service.uploadFiles(imageFiles, EVENT);

        EventEntity event = EventEntity.builder()
                .accommodationEntity(accommodationEntity)
                .eventName(eventCreateRequest.getEventName())
                .detail(eventCreateRequest.getDetail())
                .startDate(eventCreateRequest.getStartDate())
                .endDate(eventCreateRequest.getEndDate())
                .build();

        eventRepository.save(event);
        addImagesToEvent(event, imageUrls);

        return event;
    }

    public EventView readEvent(Long id) {

        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(EVENT_NOT_FOUND));

        List<String> images = event.getImages().stream()
                .map(EventEntity.ImageEntity::getUrl)
                .collect(Collectors.toList());

        return EventView.builder()
                .id(event.getId())
                .accommodationName(event.getAccommodationEntity().getName())
                .images(images)
                .eventName(event.getEventName())
                .detail(event.getDetail())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .build();

    }

    @Transactional(readOnly = true)
    public List<EventView> readEvents(EventFindQuery query) {

        List<EventEntity> eventEntities = eventRepository.findAllEvents(query);

        if (eventEntities.isEmpty()) {
            throw new EasyCheckException(EVENTS_NOT_FOUND);
        }

        List<EventView> eventViews = eventEntities.stream()
                .map(eventEntity -> {
                    List<String> imageUrls = eventEntity.getImages().stream()
                            .map(EventEntity.ImageEntity::getUrl)
                            .collect(Collectors.toList());

                    return new EventView(
                            eventEntity.getId(),
                            eventEntity.getAccommodationEntity().getName(),
                            imageUrls,
                            eventEntity.getEventName(),
                            eventEntity.getDetail(),
                            eventEntity.getStartDate(),
                            eventEntity.getEndDate()
                    );
                })
                .collect(Collectors.toList());

        return eventViews;

    }

    @Transactional
    public void updateEvent(Long id, @NotNull EventUpdateRequest eventUpdateRequest) {

        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new EasyCheckException(EVENT_NOT_FOUND));

        AccommodationEntity accommodationEntity = accommodationRepository.findById(eventUpdateRequest.getAccommodationId())
                .orElseThrow(() -> new EasyCheckException(ACCOMMODATION_NOT_FOUND));

        if (eventUpdateRequest.getEventName() == null || eventUpdateRequest.getDetail() == null ||
                eventUpdateRequest.getStartDate() == null || eventUpdateRequest.getEndDate() == null) {
            throw new EasyCheckException(ARGUMENT_NOT_VALID);
        }

        event.update(eventUpdateRequest, accommodationEntity);

    }

    @Transactional
    public void updateEventImage(Long imageId, MultipartFile newImageFile) {
        EventEntity.ImageEntity imageToUpdate = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new EasyCheckException(IMAGE_NOT_FOUND));

        String oldImageUrl = imageToUpdate.getUrl();

        String[] parts = oldImageUrl.split("/");

        String deleteImage = String.join("/", Arrays.copyOfRange(parts, 3, parts.length));

        s3Service.deleteFile(deleteImage);

        String newImageUrl = s3Service.uploadFile(newImageFile, ROOM);
        imageToUpdate.setUrl(newImageUrl);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        EventEntity eventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new EasyCheckException(EVENT_NOT_FOUND));

        for (EventEntity.ImageEntity image : eventEntity.getImages()) {
            String oldImageUrl = image.getUrl();

            String[] parts = oldImageUrl.split("/");
            String deleteImage = String.join("/", Arrays.copyOfRange(parts, 3, parts.length));

            s3Service.deleteFile(deleteImage);
        }

        eventRepository.delete(eventEntity);

    }
}
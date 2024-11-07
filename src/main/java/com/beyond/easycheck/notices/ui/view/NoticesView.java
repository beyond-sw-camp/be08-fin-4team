package com.beyond.easycheck.notices.ui.view;

import com.beyond.easycheck.additionalservices.infrastructure.entity.AdditionalServiceEntity;
import com.beyond.easycheck.additionalservices.ui.view.AdditionalServiceView;
import com.beyond.easycheck.notices.infrastructure.persistence.entity.NoticesEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoticesView {

    private Long id;

    private String accommodationName;

    private String userName;

    private String title;

    private String content;

    private String updatedAt;

    public static List<NoticesView> listof(List<NoticesEntity> filteredNotices) {
        return filteredNotices.stream()
                .map(NoticesView::of)
                .toList();
    }

    public static NoticesView of(NoticesEntity noticesEntity) {
        // 날짜 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = noticesEntity.getUpdatedDate().format(formatter);

        return new NoticesView(
                noticesEntity.getId(),
                noticesEntity.getAccommodationEntity().getName(),
                noticesEntity.getUserEntity().getName(),
                noticesEntity.getTitle(),
                noticesEntity.getContent(),
                formattedDateTime  // 포맷된 날짜 추가
        );
    }
}
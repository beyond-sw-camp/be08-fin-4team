package com.beyond.easycheck.user.application.service;

import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import lombok.Builder;

import java.sql.Timestamp;

public interface UserReadUseCase {

    void checkEmailDuplicated(UserFindQuery query);

    FindUserResult getUserInfo(UserFindQuery query);

    @Builder
    record UserFindQuery(
            Long userId,
            String email
    ) {

        public UserFindQuery(Long userId) {
            this(userId, null);
        }
    }

    record FindUserResult(
            Long id,
            String email,
            String name,
            String phone,
            String addr,
            String addrDetail,
            String status,
            char marketingConsent,
            int point,
            String role,
            Timestamp createdDate,
            Timestamp updatedDate
    ) {
        public static FindUserResult findByUserEntity(UserEntity userEntity) {
            return new FindUserResult(
                    userEntity.getId(),
                    userEntity.getEmail(),
                    userEntity.getName(),
                    userEntity.getPhone(),
                    userEntity.getAddr(),
                    userEntity.getAddrDetail(),
                    userEntity.getStatus().name(),
                    userEntity.getMarketingConsent(),
                    userEntity.getPoint(),
                    userEntity.getRole().getName(),
                    userEntity.getCreatedDate(),
                    userEntity.getUpdatedDate()
            );
        }
    }

    record FindJwtResult(
            String accessToken,
            String refreshToken
    ) {
        public static FindJwtResult findByTokenString(String accessToken, String refreshToken) {
            return new FindJwtResult(accessToken, refreshToken);
        }
    }
}

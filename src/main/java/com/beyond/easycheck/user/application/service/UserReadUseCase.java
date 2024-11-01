package com.beyond.easycheck.user.application.service;

import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.QUserEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.Optional;

public interface UserReadUseCase {

    FindUserResult getUserPoint();

    void checkEmailDuplicated(UserFindQuery query);

    FindUserResult getUserInfo(UserFindQuery query);

    FindUserResult findUserByNameAndPhone(UserFindQuery query);

    @Builder
    record UserFindQuery(
            Long userId,
            String email,
            String phone
    ) {

        public UserFindQuery(Long userId) {
            this(userId, null, null);
        }
    }

    @Builder
    record FindUserResult(
            Long id,
            String email,
            String name,
            String phone,
            String addr,
            String addrDetail,
            String status,
            Character marketingConsent,
            Integer point,
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

        public static FindUserResult findByUserEntityEmail(UserEntity userEntity) {
            return FindUserResult.builder().email(userEntity.getEmail()).build();
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

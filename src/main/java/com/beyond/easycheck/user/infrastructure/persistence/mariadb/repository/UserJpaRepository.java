package com.beyond.easycheck.user.infrastructure.persistence.mariadb.repository;

import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(
            attributePaths = {
                    "userPermissions",
                    "userPermissions.permission",
                    "role"
            })
    Optional<UserEntity> findUserEntityByEmail(String email);

    // 아이디(이메일) 찾기
    Optional<UserEntity> findUserEntityByNameAndPhone(String name, String phone);
}

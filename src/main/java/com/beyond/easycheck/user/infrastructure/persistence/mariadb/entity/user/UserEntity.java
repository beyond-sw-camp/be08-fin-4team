package com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user;

import com.beyond.easycheck.permissions.infrastructure.persistence.mariadb.entity.UserPermissionEntity;
import com.beyond.easycheck.user.application.domain.UserStatus;
import com.beyond.easycheck.user.application.service.UserOperationUseCase;
import com.beyond.easycheck.user.application.service.UserOperationUseCase.UserRegisterCommand;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.corporate.CorporateEntity;
import com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.role.RoleEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;

import static com.beyond.easycheck.user.application.service.UserOperationUseCase.*;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
@ToString(exclude = {"password", "createdDate", "updatedDate", "userPermissions", "corporate"})
public class  UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    private String phone;

    private String addr;

    private String addrDetail;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Character marketingConsent;

    private Integer point;

    @CreationTimestamp
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp updatedDate;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @OneToMany(mappedBy = "user")
    private List<UserPermissionEntity> userPermissions;

    @OneToOne(mappedBy = "user")
    private CorporateEntity corporate;

    private UserEntity(String email, String name, String phone, UserStatus status, String addr, String addrDetail, char marketingConsent) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.addr = addr;
        this.status = status;
        this.addrDetail = addrDetail;
        this.marketingConsent = marketingConsent;
    }

    public UserEntity(String email, String name, String phone, UserStatus status) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.status = status;
    }

    private UserEntity(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public static UserEntity createUser(UserRegisterCommand command) {
        return new UserEntity(
                command.email(),
                command.name(),
                command.phone(),
                UserStatus.ACTIVE,
                command.addr(),
                command.addrDetail(),
                command.marketingConsent()
        );
    }

    public static UserEntity createCorporateUser(CorporateUserRegisterCommand command) {
        return new UserEntity(
                command.email(),
                command.name(),
                command.phone(),
                UserStatus.PENDING
        );
    }

    public static UserEntity createGuestUser(String name, String phone) {
        return new UserEntity(name, phone);
    }

    public void updateUser(UserUpdateCommand command) {
        this.email = command.email();
        this.phone = command.phone();
        this.addr = command.addr();
        this.addrDetail = command.addrDetail();
    }

    public void accumulatePoints(int amount) {
        // 최대 적립 포인트 요구사항이 생기면 검증로직 추가해야함
        this.point += amount;
    }

    public void usePoints(int amount) {
        // 사용 금액 유효성 검사
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }

        // 최소 사용 가능 포인트 검사 (필요한 경우)
        final int MIN_USABLE_POINTS = 1000; // 예시: 1000포인트 이상부터 사용 가능
        if (amount < MIN_USABLE_POINTS) {
            throw new IllegalArgumentException("최소 사용 가능 포인트는 " + MIN_USABLE_POINTS + "입니다.");
        }

        // 보유 포인트 부족 검사
        if (this.point < amount) {
            throw new IllegalStateException("포인트가 부족합니다. 현재 포인트: " + this.point);
        }

        this.point -= amount;

        // 포인트 사용 이력 저장 (필요한 경우)
        // savePointUsageHistory(amount);
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public void setUserStatus(UserStatus status) {
        this.status = status;
    }

    public void setUserPending() {
        this.status = UserStatus.PENDING;
    }

    public void setSecurePassword(String password) {
        this.password = password;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}

package com.beyond.easycheck.user.infrastructure.persistence.mariadb.entity.user;

import com.beyond.easycheck.permissions.infrastructure.persistence.mariadb.entity.UserPermissionEntity;
import com.beyond.easycheck.user.application.domain.UserStatus;
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

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
@ToString(of = {"id", "email", "name", "phone", "addr", "addrDetail", "marketingConsent"})
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

    private char marketingConsent;

    private int point;

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

    public static UserEntity createCorporateUser(UserRegisterCommand command) {
        return new UserEntity(
                command.email(),
                command.name(),
                command.phone(),
                UserStatus.PENDING,
                command.addr(),
                command.addrDetail(),
                command.marketingConsent()
        );
    }

    public static UserEntity createGuestUser(String name, String phone) {
        return new UserEntity(name, phone);
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public void setUserActive() {
        this.status = UserStatus.ACTIVE;
    }

    public void setUserPending() {
        this.status = UserStatus.PENDING;
    }

    public void setSecurePassword(String password) {
        this.password = password;
    }
}

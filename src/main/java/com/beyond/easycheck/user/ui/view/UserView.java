package com.beyond.easycheck.user.ui.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import static com.beyond.easycheck.user.application.service.UserReadUseCase.FindUserResult;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserView {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final String addr;
    private final String addrDetail;
    private final String status;
    private final Character marketingConsent;
    private final Integer point;
    private final String role;

    public UserView(FindUserResult user) {
        this.id = user.id();
        this.email = user.email();
        this.name = user.name();
        this.phone = user.phone();
        this.addr = user.addr();
        this.addrDetail = user.addrDetail();
        this.status = user.status();
        this.marketingConsent = user.marketingConsent();
        this.point = user.point();
        this.role = user.role();
    }
}

package com.beyond.easycheck.themeparks.ui.requestbody;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ThemeParkCreateRequest {

    @NotBlank(message = "테마파크 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "테마파크 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "테마파크 위치는 필수입니다.")
    private String location;

    private String image;
}

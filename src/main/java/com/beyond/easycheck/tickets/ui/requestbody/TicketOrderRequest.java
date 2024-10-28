package com.beyond.easycheck.tickets.ui.requestbody;

import com.beyond.easycheck.tickets.infrastructure.entity.CollectionAgreementType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderRequest {

    @NotEmpty(message = "티켓 ID 목록은 필수입니다.")
    private List<@NotNull(message = "티켓 ID는 필수입니다.") Long> ticketIds;

    @NotEmpty(message = "수량 목록은 필수입니다.")
    private List<@Positive(message = "수량은 0보다 커야 합니다.") Integer> quantities;

    @NotNull(message = "개인정보 수집 동의는 필수입니다.")
    private CollectionAgreementType collectionAgreement;

}

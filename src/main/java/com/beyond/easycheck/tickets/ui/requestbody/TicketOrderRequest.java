package com.beyond.easycheck.tickets.ui.requestbody;

import com.beyond.easycheck.tickets.infrastructure.entity.CollectionAgreementType;
import com.beyond.easycheck.tickets.infrastructure.entity.ReceiptMethodType;
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

    @NotNull(message = "티켓 ID는 필수입니다.")
    private Long ticketId;

    @Positive(message = "수량은 0보다 커야 합니다.")
    private int quantity;

    @NotNull(message = "수령 방법은 필수입니다.")
    private ReceiptMethodType receiptMethod;

    @NotNull(message = "개인정보 수집 동의는 필수입니다.")
    private CollectionAgreementType collectionAgreement;


}

package com.beyond.easycheck.tickets.ui.requestbody;

import com.beyond.easycheck.tickets.infrastructure.entity.CollectionAgreementType;
import com.beyond.easycheck.tickets.infrastructure.entity.ReceiptMethodType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // 새로운 필드 추가
    @NotEmpty(message = "구매자 이름은 필수입니다.")
    private String buyerName;

    @NotEmpty(message = "구매자 전화번호는 필수입니다.")
    private String buyerPhone;

    @NotEmpty(message = "구매자 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String buyerEmail;
}

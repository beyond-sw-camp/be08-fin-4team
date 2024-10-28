package com.beyond.easycheck.payments.ui.requestbody;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookRequest {

    private String impUid;         // PortOne에서 전송되는 결제 고유 ID
    private String status;         // 결제 상태 (e.g., "paid", "ready")
    private Integer amount;        // 결제 금액
    private String payMethod;      // 결제 수단 (e.g., "card", "vbank")
    private String merchantUid;
}

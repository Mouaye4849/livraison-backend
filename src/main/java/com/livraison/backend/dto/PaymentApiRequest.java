package com.livraison.backend.dto;

import lombok.Data;

@Data
public class PaymentApiRequest {
    private String clientPhone;
    private String passcode;
    private String operationId;
    private String amount;
    private String language;
}

package com.livraison.backend.dto;

import lombok.Data;

@Data
public class PaymentApiResponse {

    private String errorCode;
    private String errorMessage;
    private String transactionId;
}
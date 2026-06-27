package com.livraison.backend.dto;

import com.livraison.backend.entity.TypePaiement;
import lombok.Data;

@Data
public class PaiementRequestDTO {
    private TypePaiement typePaiement;
    private String clientPhone;
    private String passcode;
}
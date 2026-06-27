package com.livraison.backend.service;
import com.livraison.backend.dto.PaiementDTO;
import com.livraison.backend.entity.TypePaiement;
import java.util.List;
import java.util.UUID;
public interface PaiementService {
    PaiementDTO payer( UUID colisId, TypePaiement typePaiement, String clientPhone, String passcode );
    List<PaiementDTO> getMyPayments();
    Double getAdminRevenue();
    long getTotalTransactions();
}
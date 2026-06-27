package com.livraison.backend.controller;

import com.livraison.backend.dto.PaiementDTO;
import com.livraison.backend.dto.PaiementRequestDTO;
import com.livraison.backend.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping("/{colisId}")

    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<PaiementDTO> payer(

            @PathVariable UUID colisId,

            @RequestBody PaiementRequestDTO request
    ) {

        return ResponseEntity.ok(

                paiementService.payer(
                        colisId,
                        request.getTypePaiement(),
                        request.getClientPhone(),
                        request.getPasscode()
                )
        );
    }


    @GetMapping("/me")

    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<List<PaiementDTO>> getMyPayments() {

        return ResponseEntity.ok(
                paiementService.getMyPayments()
        );
    }

    @GetMapping("/admin/revenue")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Double> getRevenue() {

        return ResponseEntity.ok(
                paiementService.getAdminRevenue()
        );
    }


    @GetMapping("/admin/transactions")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Long> getTransactions() {

        return ResponseEntity.ok(
                paiementService.getTotalTransactions()
        );
    }
}

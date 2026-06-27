package com.livraison.backend.service;

import com.livraison.backend.dto.ColisRequestDTO;
import com.livraison.backend.dto.ColisResponseDTO;
import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.TrackingStatus;

import java.util.List;
import java.util.UUID;

public interface ColisService {

    ColisResponseDTO createColis(ColisRequestDTO dto);

    List<ColisResponseDTO> getAllColis();

    ColisResponseDTO getColisById(UUID id);

    ColisResponseDTO updateColis(UUID id, ColisRequestDTO dto);

    ColisResponseDTO deleteColis(UUID id);

    ColisResponseDTO publishColis(UUID id);

    ColisResponseDTO startColis(UUID id);

    ColisResponseDTO finishColis(UUID id);

    ColisResponseDTO cancelColis(UUID id);

    List<ColisResponseDTO> getMyColis(String email);

    ColisResponseDTO assignTrajet(UUID colisId, UUID trajetId);
    List<ColisResponseDTO> getAvailableColisForVoyageur(UUID trajetId);
    List<ColisResponseDTO> getPublicColis();
    void createTracking(
            Colis colis,
            TrackingStatus status,
            String message,
            String location
    );

}
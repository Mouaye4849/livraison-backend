package com.livraison.backend.service;

import com.livraison.backend.dto.TrajetRequestDTO;
import com.livraison.backend.dto.TrajetResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TrajetService {

    TrajetResponseDTO createTrajet(TrajetRequestDTO dto);

    List<TrajetResponseDTO> getOpenTrajets();

    void acceptColis(UUID trajetId, UUID colisId);

    List<TrajetResponseDTO> getMyTrajets();
    List<TrajetResponseDTO> getAllTrajets();
    TrajetResponseDTO getTrajetById(UUID id);
    TrajetResponseDTO updateTrajet(UUID id, TrajetRequestDTO dto);
    void deleteTrajet(UUID id);
    List<TrajetResponseDTO> getPendingTrajets();

    void approveTrajet(UUID id);

    void rejectTrajet(UUID id);
}
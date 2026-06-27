package com.livraison.backend.service;

import com.livraison.backend.dto.GpsLocationDTO;

import java.util.UUID;

public interface GpsService {

    GpsLocationDTO updateLocation(UUID colisId, Double latitude, Double longitude);

    GpsLocationDTO getLatestLocation(UUID colisId);
}

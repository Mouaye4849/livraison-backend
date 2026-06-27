package com.livraison.backend.service;

import com.livraison.backend.dto.PhotoColisDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface PhotoColisService {

    PhotoColisDTO uploadPhoto(UUID colisId, MultipartFile file);

    List<PhotoColisDTO> getPhotosByColis(UUID colisId);

    void deletePhoto(UUID photoId);
}
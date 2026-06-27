package com.livraison.backend.service;

import com.livraison.backend.dto.PhotoColisDTO;
import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.PhotoColis;
import com.livraison.backend.exception.BusinessException;
import com.livraison.backend.exception.ResourceNotFoundException;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.PhotoColisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoColisServiceImpl implements PhotoColisService {

    private final PhotoColisRepository photoColisRepository;
    private final ColisRepository colisRepository;

    private final String uploadDir = "uploads/";

    @Override
    public PhotoColisDTO uploadPhoto(UUID colisId, MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/jpg"))) {

            throw new BusinessException("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("File too large (max 5MB)");
        }

        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new ResourceNotFoundException("Colis not found"));

        try {

            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "_" + originalName;

            Path path = Paths.get(uploadDir, filename);

            Files.createDirectories(path.getParent());

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            PhotoColis photo = PhotoColis.builder()
                    .filename(filename)
                    .colis(colis)
                    .build();

            photoColisRepository.save(photo);

            return PhotoColisDTO.builder()
                    .id(photo.getId())
                    .url("http://localhost:8080/uploads/" + filename)
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error uploading photo");
        }
    }

    @Override
    public List<PhotoColisDTO> getPhotosByColis(UUID colisId) {

        return photoColisRepository.findByColisId(colisId)
                .stream()
                .map(photo -> PhotoColisDTO.builder()
                        .id(photo.getId())
                        .url("/uploads/" + photo.getFilename())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhoto(UUID photoId) {

        PhotoColis photo = photoColisRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        try {
            Path path = Paths.get(uploadDir + photo.getFilename());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file");
        }

        photoColisRepository.delete(photo);
    }
}
package com.livraison.backend.service;

import com.livraison.backend.dto.UserResponseDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AdminService {

    Page<UserResponseDTO> getUsers(int page, int size);

    UserResponseDTO getUserById(UUID id);

    UserResponseDTO updateUser(UUID id, UserResponseDTO dto);

    void disableUser(UUID id);

    void enableUser(UUID id);

    void promoteToAdmin(UUID id);

    void demoteToUser(UUID id);

    void deleteUser(UUID id);

    public long countColis();
    public long countTrajets();
}
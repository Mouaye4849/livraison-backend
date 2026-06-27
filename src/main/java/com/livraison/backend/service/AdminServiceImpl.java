package com.livraison.backend.service;

import com.livraison.backend.dto.UserResponseDTO;
import com.livraison.backend.entity.Role;
import com.livraison.backend.entity.User;
import com.livraison.backend.exception.BusinessException;
import com.livraison.backend.exception.ResourceNotFoundException;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.TrajetRepository;
import com.livraison.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TrajetRepository trajetRepository;
    private final ColisRepository colisRepository;

    // =============================
    // 📄 PAGINATED USERS
    // =============================
    @Override
    public Page<UserResponseDTO> getUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToDTO);
    }


    @Override
    public UserResponseDTO getUserById(UUID id) {
        return mapToDTO(findUser(id));
    }

    // =============================
    // ✏️ UPDATE USER
    // =============================
    @Override
    public UserResponseDTO updateUser(UUID id, UserResponseDTO dto) {
        User user = findUser(id);

        user.setEmail(dto.getEmail());

        return mapToDTO(userRepository.save(user));
    }

    // =============================
    // 🛑 DISABLE
    // =============================
    @Override
    public void disableUser(UUID id) {
        User user = findUser(id);

        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new BusinessException("Impossible de désactiver un ADMIN");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    // =============================
    // ✅ ENABLE
    // =============================
    @Override
    public void enableUser(UUID id) {
        User user = findUser(id);

        user.setEnabled(true);
        userRepository.save(user);
    }

    // =============================
    // 🔐 PROMOTE
    // =============================
    @Override
    public void promoteToAdmin(UUID id) {
        User user = findUser(id);

        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    // =============================
    // 🔽 DEMOTE
    // =============================
    @Override
    public void demoteToUser(UUID id) {
        User user = findUser(id);

        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new BusinessException("User is not ADMIN");
        }

        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    // =============================
    // ❌ DELETE USER
    // =============================
    @Override
    public void deleteUser(UUID id) {

        User user = findUser(id);

        // ❌ منع حذف Admin
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new BusinessException("Impossible de supprimer un ADMIN");
        }

        // ❌ منع حذف Voyageur عنده trajets
        if (user.getRole() == Role.ROLE_VOYAGEUR &&
                trajetRepository.existsByVoyageur(user)) {

            throw new BusinessException("Voyageur possède des trajets");
        }

        userRepository.delete(user);
    }

    @Override
    public long countColis() {
        return colisRepository.count();
    }

    @Override
    public long countTrajets() {
        return trajetRepository.count();
    }

    // =============================
    // 🔧 HELPERS
    // =============================
    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .build();
    }
}
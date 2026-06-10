package com.athenura.contentflow.user.service;

import com.athenura.contentflow.user.dto.UserUpdateDTO;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateProfile(String email, UserUpdateDTO updateDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDTO.getName() != null) user.setName(updateDTO.getName());
        if (updateDTO.getProfilePhotoUrl() != null) user.setProfilePhotoUrl(updateDTO.getProfilePhotoUrl());

        return userRepository.save(user);
    }
    @Transactional
    public void removeProfilePhoto(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfilePhotoUrl() != null) {
            try {
                Files.deleteIfExists(
                        Paths.get("uploads", user.getProfilePhotoUrl())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        user.setProfilePhotoUrl(null);
        userRepository.save(user);
    }

    @Transactional
    public String uploadProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uploadDir = "uploads";
        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try {
            Files.copy(file.getInputStream(), Paths.get(uploadDir + File.separator + fileName));
            user.setProfilePhotoUrl(fileName);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Could not store file", e);
        }
        return fileName;
    }
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
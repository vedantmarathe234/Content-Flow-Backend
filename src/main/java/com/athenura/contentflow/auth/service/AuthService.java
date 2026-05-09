package com.athenura.contentflow.auth.service;

import com.athenura.contentflow.auth.dto.RegistrationRequestDTO;
import com.athenura.contentflow.entity.User;
import com.athenura.contentflow.enums.Role;
import com.athenura.contentflow.repository.UserReopsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserReopsitory userRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(RegistrationRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .build();

        userRepository.save(user);
        return "User register successfully";
    }
}

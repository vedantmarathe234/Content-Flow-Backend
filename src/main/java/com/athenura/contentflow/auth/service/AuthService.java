package com.athenura.contentflow.auth.service;

import com.athenura.contentflow.auth.dto.AuthResponseDTO;
import com.athenura.contentflow.auth.dto.LoginRequestDTO;
import com.athenura.contentflow.auth.dto.RegistrationRequestDTO;
import com.athenura.contentflow.auth.security.JwtUtil;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.user.repository.UserReopsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {



    private final UserReopsitory userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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


    public AuthResponseDTO login(LoginRequestDTO request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if(!passwordMatches){
            throw  new RuntimeException("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

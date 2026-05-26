package com.athenura.contentflow.auth.service;

import com.athenura.contentflow.auth.dto.*;
import com.athenura.contentflow.auth.security.JwtUtil;
import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.department.repository.DepartmentRepository;
import com.athenura.contentflow.email.dto.EmailRequest;
import com.athenura.contentflow.email.service.EmailService;
import com.athenura.contentflow.user.dto.UserResponseDTO;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Value("${app.security.admin-master-key:ADMIN@athenura123}")
    private String masterAdminKey;
    private final EmailService emailService;

    public String register(RegistrationRequestDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if ("ADMIN".equalsIgnoreCase(dto.getRole())) {

            if (!masterAdminKey.equals(dto.getSecretKey())) {
                throw new RuntimeException("Invalid Admin Secret Key! Access Denied.");
            }
            user.setRole(Role.ADMIN);
            user.setDepartment(null);

            userRepository.save(user);
            return "Admin Registered Successfully!";
        } else {

            Department dept = departmentRepository.findByName(dto.getDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found!"));

            if (!dept.getSecretKey().equals(dto.getSecretKey())) {
                throw new RuntimeException("Invalid Secret Key for the selected department!");
            }
            user.setRole(Role.INTERN);
            user.setDepartment(dept);

            userRepository.save(user);
            return "Intern Registered Successfully!";
        }


    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with provided email!"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password!");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : "All Access")
                .build();
    }

    public UserResponseDTO getUserProfile(String token) {

        String jwt = token.substring(7);
        String email = jwtUtil.extractEmail(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());


        if (user.getDepartment() != null) {
            response.setDepartmentName(user.getDepartment().getName());
        }


        return response;
    }


    public String forgotPassword(
            ForgotPasswordRequestDTO request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found!"
                        ));

        String token =
                UUID.randomUUID().toString();

        user.setResetToken(token);

        userRepository.save(user);

        String resetLink =
                "http://localhost:5173/reset-password?token="
                        + token;

        EmailRequest emailRequest =
                new EmailRequest();

        emailRequest.setTo(user.getEmail());

        emailRequest.setSubject(
                "Reset Your Password"
        );

        emailRequest.setBody(
                "<h2>Password Reset</h2>"
                        + "<p>Click below link to reset your password:</p>"
                        + "<a href='"
                        + resetLink
                        + "'>Reset Password</a>"
        );

        emailService.sendEmail(emailRequest);

        return "Reset link sent to email";
    }


    public String resetPassword(
            ResetPasswordRequestDTO request
    ) {

        User user = userRepository
                .findByResetToken(
                        request.getResetToken()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid reset token!"
                        ));

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setResetToken(null);

        userRepository.save(user);

        return "Password reset successful";
    }
}
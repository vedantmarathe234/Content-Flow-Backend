package com.athenura.contentflow.auth.controller;

import com.athenura.contentflow.auth.dto.*;
import com.athenura.contentflow.auth.service.AuthService;
import com.athenura.contentflow.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegistrationRequestDTO request){

        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request){
        return authService.login(request);
    }

    @GetMapping("/profile")
    public UserResponseDTO getCurrentUser(@RequestHeader("Authorization") String token) {
        return authService.getUserProfile(token);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequestDTO request
    ) {

        return authService.forgotPassword(request);
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestBody ChangePasswordRequestDTO request,
            @RequestHeader("Authorization") String token
    ) {
        return authService.changePassword(
                request,
                token
        );
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestBody ResetPasswordRequestDTO request
    ) {

        return authService.resetPassword(request);
    }
}
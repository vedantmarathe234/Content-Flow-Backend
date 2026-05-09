package com.athenura.contentflow.auth.controller;

import com.athenura.contentflow.auth.dto.RegistrationRequestDTO;
import com.athenura.contentflow.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegistrationRequestDTO request){
        return authService.register(request);
    }
}

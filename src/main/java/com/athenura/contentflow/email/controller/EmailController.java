package com.athenura.contentflow.email.controller;

import com.athenura.contentflow.email.dto.EmailRequest;
import com.athenura.contentflow.email.service.EmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(
            EmailService emailService
    ) {

        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestBody EmailRequest request
    ) {

        return ResponseEntity.ok(
                emailService.sendEmail(request)
        );
    }
}
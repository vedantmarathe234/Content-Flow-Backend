package com.athenura.contentflow.content.controller;

import com.athenura.contentflow.content.dto.ApiResponse;
import com.athenura.contentflow.content.entity.Notification;
import com.athenura.contentflow.content.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            Principal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        principal.getName()
                )
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id)
        );
    }
}

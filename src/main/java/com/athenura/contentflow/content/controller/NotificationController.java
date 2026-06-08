package com.athenura.contentflow.content.controller;

import com.athenura.contentflow.content.dto.ApiResponse;
import com.athenura.contentflow.content.dto.RecentActivityResponse;
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

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllAsRead(
            Principal principal
    ) {
        return ResponseEntity.ok(
                notificationService.markAllAsRead(
                        principal.getName()
                )
        );
    }

    @PutMapping("/content/{contentId}/read")
    public ResponseEntity<ApiResponse> markByContent(
            @PathVariable Long contentId,
            Principal principal
    ) {

        System.out.println("MARK CONTENT READ HIT");
        System.out.println("CONTENT ID = " + contentId);
        System.out.println("USER = " + principal.getName());

        notificationService.markNotificationsByContent(
                contentId,
                principal.getName()
        );

        return ResponseEntity.ok(
                new ApiResponse(
                        "Notifications marked as read"
                )
        );
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentActivityResponse>>
    getRecentActivity() {

        return ResponseEntity.ok(
                notificationService.getRecentActivity()
        );
    }

}

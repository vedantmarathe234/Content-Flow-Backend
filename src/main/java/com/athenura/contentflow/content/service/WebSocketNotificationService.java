package com.athenura.contentflow.content.service;

import com.athenura.contentflow.content.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(
            Long userId,
            Notification notification
    ) {

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                notification
        );
    }
}

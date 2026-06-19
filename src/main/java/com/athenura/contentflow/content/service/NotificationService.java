package com.athenura.contentflow.content.service;

import com.athenura.contentflow.content.dto.ApiResponse;
import com.athenura.contentflow.content.dto.RecentActivityResponse;
import com.athenura.contentflow.content.entity.Notification;
import com.athenura.contentflow.content.repository.ContentRepository;
import com.athenura.contentflow.content.repository.NotificationRepository;
import com.athenura.contentflow.exception.ResourceNotFoundException;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    public List<Notification> getMyNotifications(String email) {

//        System.out.println("EMAIL = " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

//        System.out.println("USER ID = " + user.getId());
//        System.out.println("USER NAME = " + user.getName());

        List<Notification> notifications =
                notificationRepository.findByUserOrderByCreatedAtDesc(user);

        notifications.forEach(notification -> {
            if (notification.getContentId() != null) {
                contentRepository.findById(notification.getContentId()).ifPresent(content -> {
                    if (content.getTeam() != null) {
                        notification.setTeamName(content.getTeam().getName());
                        if (content.getTeam().getDepartment() != null) {
                            notification.setDepartmentName(content.getTeam().getDepartment().getName());
                        }
                    } else if (content.getCreatedBy() != null && content.getCreatedBy().getDepartment() != null) {
                        notification.setDepartmentName(content.getCreatedBy().getDepartment().getName());
                    }
                });
            }
        });

//        System.out.println("COUNT = " + notifications.size());

        return notifications;
    }

    public ApiResponse markAsRead(Long id) {

        Notification notification = notificationRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);

        return new ApiResponse("Notification marked as read");
    }

    public ApiResponse markAllAsRead(String email) {

        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(user);

        notifications.forEach(notification -> notification.setRead(true)
        );
        notificationRepository.saveAll(notifications);

        return new ApiResponse("All notifications marked as read");
    }

    @Transactional
    public void markNotificationsByContent(
            Long contentId,
            String email
    ) {

//        System.out.println("===========");
//        System.out.println("CONTENT ID = " + contentId);
//        System.out.println("EMAIL = " + email);

        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository
                        .findByUserAndContentIdAndIsReadFalse(user, contentId);
//        System.out.println("FOUND NOTIFICATIONS = " + notifications.size());

        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public List<RecentActivityResponse> getRecentActivity() {
        return notificationRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(notification -> {
                    RecentActivityResponse response = new RecentActivityResponse();
                    response.setMessage(notification.getMessage());
                    response.setCreatedAt(notification.getCreatedAt());

                    if (notification.getContentId() != null) {
                        contentRepository.findById(notification.getContentId()).ifPresent(content -> {
                            if (content.getCreatedBy() != null) {
                                response.setCreatorId(content.getCreatedBy().getId());
                            }
                            if (content.getTeam() != null) {
                                response.setTeamName(content.getTeam().getName());
                                if (content.getTeam().getDepartment() != null) {
                                    response.setDepartmentName(content.getTeam().getDepartment().getName());
                                }
                            }
                        });
                    }

                    return response;
                })
                .toList();
    }


}

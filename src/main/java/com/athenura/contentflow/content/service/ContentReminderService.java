package com.athenura.contentflow.content.service;

import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.content.entity.Content;
import com.athenura.contentflow.content.repository.ContentRepository;
import com.athenura.contentflow.email.service.EmailNotificationService;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentReminderService {

    private final ContentRepository contentRepository;
    private final EmailNotificationService emailNotificationService;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional(readOnly = true)
    public void sendPendingLeaderReminders() {

        LocalDateTime threshold = LocalDateTime.now().minusHours(36);
        List<Content> contents = contentRepository.findByReminderSentFalseAndCreatedAtBefore(threshold);

        if (contents.isEmpty()) {
            return;
        }

        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (Content content : contents) {
            if (content.getStatus() == ContentStatus.APPROVED || content.getStatus() == ContentStatus.REJECTED) {
                continue;
            }

            boolean isIndividual = (content.getTeam() == null);

            if (content.getStatus() == ContentStatus.PENDING_LEADER) {
                if (!isIndividual && content.getTeam().getTeamLeader() != null) {
                    emailNotificationService.sendTeamPendingReminder(content);
                    content.setReminderSent(true);
                    contentRepository.save(content);
                }
                continue;
            }


            if (content.getStatus() == ContentStatus.PENDING) {
                for (User admin : admins) {
                    emailNotificationService.sendAdminPendingReminder(content, admin);
                }

                if (isIndividual) {
                    emailNotificationService.sendCreatorPendingReminder(content);
                }
                content.setReminderSent(true);
                contentRepository.save(content);
            }
        }
    }
}
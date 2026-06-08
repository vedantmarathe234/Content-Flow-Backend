package com.athenura.contentflow.content.service;

import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.content.entity.Content;
import com.athenura.contentflow.content.repository.ContentRepository;
import com.athenura.contentflow.email.dto.EmailRequest;
import com.athenura.contentflow.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentReminderService {

    private final ContentRepository contentRepository;
    private final EmailService emailService;

//    @Scheduled(fixedRate = 10000)
    public void sendPendingLeaderReminders() {

        LocalDateTime threshold =
                LocalDateTime.now().minusSeconds(15);

        List<Content> pendingContents =
                contentRepository
                        .findByStatusAndCreatedAtBefore(
                                ContentStatus.PENDING,
                                threshold
                        );

        for (Content content : pendingContents) {

            if(content.getTeam() == null
                    || content.getTeam().getTeamLeader() == null){
                continue;
            }

            String leaderEmail =
                    content.getTeam()
                            .getTeamLeader()
                            .getEmail();

            EmailRequest request =
                    EmailRequest.builder()
                            .to(leaderEmail)
                            .subject("Content Pending For Review")
                            .body(
                                    "<h2>Reminder: Content Pending Review</h2>" +
                                            "<p><b>Title:</b> "
                                            + content.getTitle() + "</p>" +
                                            "<p>This content is pending for more than 36 hours.</p>"
                            )
                            .build();

            emailService.sendEmail(request);
            System.out.println(
                    "Reminder running for content id: "
                            + content.getId()
            );

        }
    }
}
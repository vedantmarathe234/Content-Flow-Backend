package com.athenura.contentflow.email.service;

import com.athenura.contentflow.content.entity.Content;
import com.athenura.contentflow.email.dto.EmailRequest;
import com.athenura.contentflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final EmailService emailService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    private String formatCreationTime(Content content) {
        if (content.getCreatedAt() != null) {
            return content.getCreatedAt().format(formatter);
        }
        return "N/A";
    }

    private String buildBaseEmail(String headerTitle, String bannerColor, String greetingText, String mainDescription, String infoTableRows, String actionCalloutHtml) {
        return "<div style=\"font-family: 'Segoe UI', system-ui, Helvetica, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.02); background-color: #ffffff;\">"
                + "  <div style=\"background: " + bannerColor + "; padding: 26px 24px; text-align: center;\">"
                + "    <h2 style=\"color: #ffffff; margin: 0; font-size: 20px; font-weight: 700; letter-spacing: -0.5px;\">" + headerTitle + "</h2>"
                + "  </div>"
                + "  <div style=\"padding: 28px 24px; color: #334155; font-size: 14px; line-height: 1.6;\">"
                + "    <p style=\"margin-top: 0; font-size: 16px; font-weight: 700; color: #0f172a;\">" + greetingText + "</p>"
                + "    <p style=\"margin-top: 4px; color: #475569; font-size: 14px;\">" + mainDescription + "</p>"
                + "    <table style=\"width: 100%; border-collapse: separate; border-spacing: 0; margin: 24px 0; border: 1px solid #f1f5f9; border-radius: 12px; overflow: hidden;\">"
                +        infoTableRows
                + "    </table>"
                +      actionCalloutHtml
                + "    <div style=\"margin-top: 32px; padding-top: 18px; border-top: 1px solid #f1f5f9; text-align: center;\">"
                + "      <p style=\"margin: 0; font-size: 10px; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px;\">Automated Email Notification</p>"
                + "      <p style=\"margin: 4px 0 0 0; font-size: 12px; color: #0D7A80; font-weight: 600;\">Powered by ContentFlow</p>"
                + "    </div>"
                + "  </div>"
                + "</div>";
    }

    private String buildTableRow(String fieldLabel, String fieldValue, boolean useSlateBg) {
        String rowBg = useSlateBg ? "#f8fafc" : "#ffffff";
        return "<tr style=\"background-color: " + rowBg + ";\">"
                + "  <td style=\"padding: 12px 16px; font-weight: 600; color: #64748b; width: 32%; border-bottom: 1px solid #f1f5f9; font-size: 13px; text-transform: uppercase; letter-spacing: 0.3px;\">" + fieldLabel + "</td>"
                + "  <td style=\"padding: 12px 16px; color: #1e293b; font-weight: 500; border-bottom: 1px solid #f1f5f9; font-size: 13px;\">" + fieldValue + "</td>"
                + "</tr>";
    }

    private String buildCallout(String text, String textColor, String borderLeftColor, String bgBoxColor) {
        return "<div style=\"background-color: " + bgBoxColor + "; border-left: 4px solid " + borderLeftColor + "; padding: 14px 16px; border-radius: 8px; margin: 16px 0; font-size: 13px; color: " + textColor + "; font-weight: 500;\">"
                +   text
                + "</div>";
    }

    private String buildRejectionButton(String redirectUrl) {
        return "<div style=\"margin-top: 20px; text-align: center;\">"
                + "  <a href=\"" + redirectUrl + "\" style=\"background-color: #dc2626; color: #ffffff; padding: 10px 22px; font-size: 13px; font-weight: 600; text-decoration: none; border-radius: 6px; display: inline-block; font-family: 'Segoe UI', sans-serif; box-shadow: 0 2px 8px rgba(0,0,0,0.06);\">"
                + "      Click here to login to ContentFlow to resubmit"
                + "  </a>"
                + "</div>";
    }

    public void sendAdminApprovalEmail(Content content) {
        boolean isIndividual = (content.getTeam() == null);

        String creatorRows = buildTableRow("Content Title", content.getTitle(), true)
                + buildTableRow("Status", "APPROVED", false)
                + buildTableRow("Assigned Team", !isIndividual ? content.getTeam().getName() : "Individual", true)
                + buildTableRow("Approved By", content.getAdminApprovedBy() != null ? content.getAdminApprovedBy() : "Admin", false)
                + buildTableRow("Date & Time", LocalDateTime.now().format(formatter), true);

        String creatorBody = buildBaseEmail(
                "✓ Content Approved",
                "linear-gradient(135deg, #10b981 0%, #059669 100%)",
                "Hello " + content.getCreatedBy().getName() + ",",
                "Your content \"" + content.getTitle() + "\" has been approved by the Administrator.",
                creatorRows,
                ""
        );

        emailService.sendEmail(
                EmailRequest.builder()
                        .to(content.getCreatedBy().getEmail())
                        .subject("Your Content Is Approved")
                        .body(creatorBody)
                        .build(),
                true
        );

        if (!isIndividual && content.getTeam().getTeamLeader() != null) {
            User leader = content.getTeam().getTeamLeader();

            if (!content.getCreatedBy().getId().equals(leader.getId())) {
                String leaderRows = buildTableRow("Content Title", content.getTitle(), true)
                        + buildTableRow("Team Member", content.getCreatedBy().getName(), false)
                        + buildTableRow("Assigned Team", content.getTeam().getName(), true)
                        + buildTableRow("Approved By", content.getAdminApprovedBy() != null ? content.getAdminApprovedBy() : "Admin", false)
                        + buildTableRow("Date & Time", LocalDateTime.now().format(formatter), true);

                String leaderBody = buildBaseEmail(
                        "✓ Team Production Approved",
                        "linear-gradient(135deg, #063A3A 0%, #0A5B63 100%)",
                        "Hello " + leader.getName() + ",",
                        "Your team member \"" + content.getCreatedBy().getName() + "\"'s content is approved by Admin.",
                        leaderRows,
                        ""
                );

                emailService.sendEmail(
                        EmailRequest.builder()
                                .to(leader.getEmail())
                                .subject("Team Content Approved: " + content.getTitle())
                                .body(leaderBody)
                                .build(),
                        false
                );
            }
        }
    }

    public void sendAdminRejectionEmail(Content content, String reason) {
        boolean isIndividual = (content.getTeam() == null);

        String creatorRows = buildTableRow("Content Title", content.getTitle(), true)
                + buildTableRow("Status", "REJECTED", false)
                + buildTableRow("Rejected By", content.getAdminApprovedBy() != null ? content.getAdminApprovedBy() : "Admin", true);

        // 🎯 TARGET THE DIRECT VIEW LINK RATHER THAN LIST PATHS
        String targetUrl = "http://localhost:5173/content/direct-view?contentId=" + content.getId();
        String adminButton = buildRejectionButton(targetUrl);

        String creatorCallout = buildCallout(
                "<strong>Reason for Rejection:</strong> " + reason,
                "#b91c1c", "#ef4444", "#fef2f2"
        ) + "<p style=\"font-size: 13px; color: #64748b; margin-top: 12px; margin-bottom: 0;\">Please update your content based on the feedback above and resubmit it for review.</p>"
                + adminButton;

        String creatorBody = buildBaseEmail(
                "✕ Content Rejected",
                "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)",
                "Hello " + content.getCreatedBy().getName() + ",",
                "Your content has been rejected by the Administrator and requires changes.",
                creatorRows,
                creatorCallout
        );

        emailService.sendEmail(
                EmailRequest.builder()
                        .to(content.getCreatedBy().getEmail())
                        .subject("Content Rejected")
                        .body(creatorBody)
                        .build(),
                true
        );

        if (!isIndividual && content.getTeam().getTeamLeader() != null) {
            User leader = content.getTeam().getTeamLeader();

            if (!content.getCreatedBy().getId().equals(leader.getId())) {
                String leaderRows = buildTableRow("Creator", content.getCreatedBy().getName(), true)
                        + buildTableRow("Content Title", content.getTitle(), false)
                        + buildTableRow("Team", content.getTeam().getName(), true)
                        + buildTableRow("Rejected By", content.getAdminApprovedBy() != null ? content.getAdminApprovedBy() : "Admin", false);

                String leaderCallout = buildCallout(
                        "<strong>Administrative Feedback:</strong> " + reason,
                        "#b91c1c", "#ef4444", "#fef2f2"
                );

                String leaderBody = buildBaseEmail(
                        "✕ Team Content Rejected",
                        "linear-gradient(135deg, #334155 0%, #1e293b 100%)",
                        "Hello " + leader.getName() + ",",
                        "Content submitted by a member of your team has been rejected by the Administrator.",
                        leaderRows,
                        leaderCallout
                );

                emailService.sendEmail(
                        EmailRequest.builder()
                                .to(leader.getEmail())
                                .subject("Team Content Rejected")
                                .body(leaderBody)
                                .build(),
                        false
                );
            }
        }
    }

    public void sendLeaderRejectionEmail(Content content, String reason) {
        String rows = buildTableRow("Content Title", content.getTitle(), true)
                + buildTableRow("Status", "REJECTED", false)
                + buildTableRow("Rejected By", content.getLeaderApprovedBy() != null ? content.getLeaderApprovedBy() : "Team Leader", true);

        // 🎯 TARGET THE DIRECT VIEW LINK RATHER THAN LIST PATHS
        String targetUrl = "http://localhost:5173/content/direct-view?contentId=" + content.getId();
        String leaderButton = buildRejectionButton(targetUrl);

        String callout = buildCallout(
                "<strong>Reason for Rejection:</strong> " + reason,
                "#b45309", "#f59e0b", "#fffbeb"
        ) + "<p style=\"font-size: 13px; color: #64748b; margin-top: 12px; margin-bottom: 0;\">Please log into the portal to edit and resubmit your content.</p>"
                + leaderButton;

        String htmlBody = buildBaseEmail(
                "✕ Content Rejected by Team Leader",
                "linear-gradient(135deg, #f59e0b 0%, #d97706 100%)",
                "Hello " + content.getCreatedBy().getName() + ",",
                "Your Team Leader has reviewed your content and requested changes.",
                rows,
                callout
        );

        emailService.sendEmail(
                EmailRequest.builder()
                        .to(content.getCreatedBy().getEmail())
                        .subject("Content Rejected By Team Leader")
                        .body(htmlBody)
                        .build(),
                true
        );
    }
    public void sendTeamPendingReminder(Content content) {
        User leader = content.getTeam().getTeamLeader();

        String rows = buildTableRow("Content Title", content.getTitle(), true)
                + buildTableRow("Submitted By", content.getCreatedBy().getName(), false)
                + buildTableRow("Submitted At", formatCreationTime(content), true)
                + buildTableRow("Time Pending", "More than 36 Hours", false);

        String callout = buildCallout(
                "⏰ <strong>Reminder:</strong> This content has been waiting for your review for over 36 hours. Please check and process it as soon as possible.",
                "#063A3A", "#0D7A80", "#0D7A80/[0.04]"
        );

        String htmlBody = buildBaseEmail(
                "⏰ Pending Review Reminder",
                "linear-gradient(135deg, #063A3A 0%, #0A5B63 100%)",
                "Hello " + leader.getName() + ",",
                "You have content waiting for your evaluation and approval.",
                rows,
                callout
        );

        emailService.sendEmail(EmailRequest.builder().to(leader.getEmail()).subject("Team Content Pending Review").body(htmlBody).build());
    }

    public void sendAdminPendingReminder(Content content, User admin) {
        String rows = buildTableRow("Content Title", content.getTitle(), true)
                + buildTableRow("Creator", content.getCreatedBy().getName(), false)
                + buildTableRow("Team", (content.getTeam() != null ? content.getTeam().getName() : "Individual"), true)
                + buildTableRow("Submitted At", formatCreationTime(content), false)
                + buildTableRow("Time Pending", "More than 36 Hours", true);

        String callout = buildCallout(
                "⚡ <strong>Reminder:</strong> This content has passed team review and is waiting for your final approval.",
                "#1e293b", "#475569", "#f1f5f9"
        );

        String htmlBody = buildBaseEmail(
                "⏰ Pending Approval Reminder",
                "linear-gradient(135deg, #1e293b 0%, #334155 100%)",
                "Hello " + admin.getName() + ",",
                "There is content waiting in your queue for final system approval.",
                rows,
                callout
        );

        emailService.sendEmail(EmailRequest.builder().to(admin.getEmail()).subject("Content Pending For Approval").body(htmlBody).build());
    }

    public void sendCreatorPendingReminder(Content content) {
        String rows = buildTableRow("Content Title", content.getTitle(), true)
                + buildTableRow("Submitted At", formatCreationTime(content), false)
                + buildTableRow("Status", "Awaiting Review", true);

        String callout = buildCallout(
                "ℹ️ <strong>Status Update:</strong> Your content has been in the review queue for over 36 hours. The reviewers have been notified.",
                "#475569", "#94a3b8", "#f8fafc"
        );

        String htmlBody = buildBaseEmail(
                "⏳ Content Review Status",
                "linear-gradient(135deg, #475569 0%, #64748b 100%)",
                "Hello " + content.getCreatedBy().getName() + ",",
                "This email confirms that your content is safely in our review queue and waiting to be processed.",
                rows,
                callout
        );

        emailService.sendEmail(EmailRequest.builder().to(content.getCreatedBy().getEmail()).subject("Your Content Is Awaiting Approval").body(htmlBody).build());
    }
}
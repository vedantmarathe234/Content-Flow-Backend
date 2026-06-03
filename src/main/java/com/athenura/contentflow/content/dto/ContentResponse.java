package com.athenura.contentflow.content.dto;

import com.athenura.contentflow.commons.enums.ContentStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class ContentResponse {
    private Long id;
    private String title;
    private String description;
    private String mediaUrl;
    private String uploadProvider;
    private ContentStatus status;
    private String rejectionReason;
    private String createdBy;
    private String team;
    private String department;
    private LocalDateTime createdAt;
    private LocalDate scheduledDate;
    private LocalDateTime actionDate;
    private String currentStage;
    private String approvedByLeader;
}
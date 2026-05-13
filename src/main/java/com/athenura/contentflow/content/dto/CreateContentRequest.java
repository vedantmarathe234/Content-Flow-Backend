package com.athenura.contentflow.content.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateContentRequest {
    private String title;
    private String description;
    private String mediaUrl;
    private String googleDriveLink;
    private String uploadProvider;
    private LocalDate scheduledDate;
}
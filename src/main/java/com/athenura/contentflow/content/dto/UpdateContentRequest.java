package com.athenura.contentflow.content.dto;

import lombok.Data;

@Data
public class UpdateContentRequest {
    private String title;
    private String description;
    private String mediaUrl;
    private String uploadProvider;
}
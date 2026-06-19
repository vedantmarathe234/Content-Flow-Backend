package com.athenura.contentflow.content.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecentActivityResponse {

    private String message;
    private LocalDateTime createdAt;
    private Long creatorId;
    private String teamName;
    private String departmentName;

}

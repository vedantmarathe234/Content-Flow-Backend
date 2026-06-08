package com.athenura.contentflow.content.dto;

import lombok.Data;

@Data
public class UserDashboardResponse {

    private long totalContent;
    private long pending;
    private long approved;
    private long rejected;
}

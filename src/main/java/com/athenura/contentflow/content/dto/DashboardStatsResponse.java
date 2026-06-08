package com.athenura.contentflow.content.dto;

import lombok.Data;

@Data
public class DashboardStatsResponse {

    private long totalContent;
    private long pendingLeader;
    private long pendingAdmin;
    private long approved;
    private long rejected;

    private long totalTeams;
    private long totalDepartments;
    private long totalUsers;
}
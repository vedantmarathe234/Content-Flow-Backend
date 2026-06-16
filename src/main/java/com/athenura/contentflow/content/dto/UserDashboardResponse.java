package com.athenura.contentflow.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDashboardResponse {

    private Long totalContent;
    private int pendingLeader;
    private int pendingAdmin;
    private int approved;
    private int rejected;
    private List<DashboardStatsResponse.DayActivityDTO> weeklyActivity;
}

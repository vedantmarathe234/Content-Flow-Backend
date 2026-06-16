package com.athenura.contentflow.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private List<DayActivityDTO> weeklyActivity;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayActivityDTO {
        private String name;
        private long count;

    }

    private List<DepartmentCountDTO> topDepartments;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepartmentCountDTO {
        private String name;
        private long count;
    }
}
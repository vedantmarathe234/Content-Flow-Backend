package com.athenura.contentflow.team.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeamResponse {
    private Long id;
    private String name;
    private String departmentName;
    private Long teamLeaderId;
    private String teamLeaderName;
    private List<String> memberNames;
}

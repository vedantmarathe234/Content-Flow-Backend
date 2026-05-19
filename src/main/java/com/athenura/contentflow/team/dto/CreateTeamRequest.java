package com.athenura.contentflow.team.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateTeamRequest {
    private String name;
    private Long departmentId;
    private Long teamLeaderId;
    private List<Long> memberIds;
}

package com.athenura.contentflow.team.dto;
import lombok.Data;
import java.util.List;

@Data
public class UpdateTeamRequest {
    private String name;
    private Long teamLeaderId;
    private List<Long> memberIds;
}

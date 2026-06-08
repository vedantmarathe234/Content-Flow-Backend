package com.athenura.contentflow.auth.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDTO {
    private Long id;
    private String token;
    private String name;
    private String email;
    private String role;
    private String departmentName;
    private boolean isTeamLeader;
}

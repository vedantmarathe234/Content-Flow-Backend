package com.athenura.contentflow.user.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private long id;
    private String name;
    private String email;
    private String role;
    private String departmentName;
    private String profilePhotoUrl;
}

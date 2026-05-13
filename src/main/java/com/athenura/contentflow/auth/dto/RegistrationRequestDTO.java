package com.athenura.contentflow.auth.dto;

import lombok.*;

@Data
public class RegistrationRequestDTO {
    private String name;
    private String email;
    private String password;
    private String role;
    private String departmentName;
    private String secretKey;
}

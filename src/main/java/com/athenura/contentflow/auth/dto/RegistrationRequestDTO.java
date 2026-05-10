package com.athenura.contentflow.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDTO {
    private String name;
    private String email;
    private String password;
    private String role;
    private String departmentName;
    private String secretKey;
}

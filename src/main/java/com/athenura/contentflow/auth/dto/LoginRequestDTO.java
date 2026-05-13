package com.athenura.contentflow.auth.dto;

import lombok.*;

@Data
public class LoginRequestDTO {

    private String email;
    private String password;
}

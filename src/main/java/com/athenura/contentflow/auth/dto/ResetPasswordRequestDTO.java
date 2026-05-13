package com.athenura.contentflow.auth.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDTO {


    private String email;

    private String resetToken;

    private String newPassword;
}

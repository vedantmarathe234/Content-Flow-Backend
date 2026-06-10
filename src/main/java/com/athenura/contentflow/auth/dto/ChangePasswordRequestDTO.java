package com.athenura.contentflow.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {
    private String currentPassword;
    private String newPassword;
}

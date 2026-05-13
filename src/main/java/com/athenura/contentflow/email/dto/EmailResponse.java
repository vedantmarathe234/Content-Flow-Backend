package com.athenura.contentflow.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmailResponse {

    private boolean success;

    private String message;
}
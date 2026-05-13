package com.athenura.contentflow.content.dto;

import lombok.Data;

@Data
public class ApprovalResponse {
    private String message;

    public ApprovalResponse(String message) {
        this.message = message;
    }
}
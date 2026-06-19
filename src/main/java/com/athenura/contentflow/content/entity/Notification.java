package com.athenura.contentflow.content.entity;

import com.athenura.contentflow.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private boolean isRead = false;

    private LocalDateTime createdAt;

    @ManyToOne
    @JsonIgnore
    private User user;

    private Long contentId;

    @Transient
    private String teamName;

    @Transient
    private String departmentName;
}
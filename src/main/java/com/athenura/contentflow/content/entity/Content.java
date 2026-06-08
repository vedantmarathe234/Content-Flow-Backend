package com.athenura.contentflow.content.entity;

import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.team.entity.Team;
import com.athenura.contentflow.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String mediaUrl;

    private String uploadProvider;


    @Enumerated(EnumType.STRING)
    @Column(name = "content_status", length = 50)
    private ContentStatus status;

    private String rejectionReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDate scheduledDate;

    private LocalDateTime actionDate;

    private String leaderApprovedBy;
    private String adminApprovedBy;

    private Boolean reminderSent = false;


    @ManyToOne
    @JoinColumn(
            name = "created_by",
            nullable = true,
            foreignKey = @ForeignKey(
                    foreignKeyDefinition =
                            "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL"
            )
    )
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(
            name = "team_id",
            nullable = true,
            foreignKey = @ForeignKey(
                    foreignKeyDefinition =
                            "FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL"
            )
    )
    private Team team;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ContentStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "public_id")
    private String publicId;

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}
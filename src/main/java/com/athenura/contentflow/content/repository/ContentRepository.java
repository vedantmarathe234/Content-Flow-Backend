package com.athenura.contentflow.content.repository;

import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(ContentStatus status);
    List<Content> findByDepartmentId(Long departmentId);
    List<Content> findByCreatedByEmail(String email);
    List<Content> findByStatusAndCreatedAtBefore(
            ContentStatus status,
            LocalDateTime createdAt
    );
    long countByStatus(ContentStatus status);

    long countByTeamId(Long teamId);

    long countByTeamIdAndStatus(
            Long teamId,
            ContentStatus status
    );

    long countByTeamIdAndCreatedById(
            Long teamId,
            Long userId
    );

    long countByTeamIdAndCreatedByIdAndStatus(
            Long teamId,
            Long userId,
            ContentStatus status
    );
}
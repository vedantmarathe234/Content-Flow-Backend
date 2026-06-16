package com.athenura.contentflow.content.repository;

import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(ContentStatus status);
    List<Content> findByDepartmentId(Long departmentId);
    List<Content> findByCreatedByEmail(String email);
    List<Content> findByTeamId(Long teamId);
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

    List<Content> findByReminderSentFalseAndCreatedAtBefore(
            LocalDateTime createdAt
    );

    @Query("SELECT FUNCTION('DAYNAME', c.createdAt) as dayName, COUNT(c.id) as totalCount " +
            "FROM Content c " +
            "WHERE c.createdAt >= :startDate " +
            "GROUP BY FUNCTION('DAYNAME', c.createdAt)")
    List<Object[]> countContentGroupedByDay(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DAYNAME', c.createdAt), COUNT(c) FROM Content c WHERE c.createdBy.id = :userId AND c.createdAt >= :startOfWeek GROUP BY FUNCTION('DAYNAME', c.createdAt)")
    List<Object[]> countUserContentGroupedByDay(@Param("userId") Long userId, @Param("startOfWeek") LocalDateTime startOfWeek);

    @Query("SELECT FUNCTION('DAYNAME', c.createdAt), COUNT(c) FROM Content c WHERE c.team.id = :teamId AND c.createdAt >= :startOfWeek GROUP BY FUNCTION('DAYNAME', c.createdAt)")
    List<Object[]> countTeamContentGroupedByDay(@Param("teamId") Long teamId, @Param("startOfWeek") LocalDateTime startOfWeek);


}
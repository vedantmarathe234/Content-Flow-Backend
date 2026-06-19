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
    List<Content> findByStatusAndCreatedAtBefore(ContentStatus status, LocalDateTime createdAt);
    List<Content> findByReminderSentFalseAndCreatedAtBefore(LocalDateTime createdAt);


    long countByStatus(ContentStatus status);
    long countByTeamId(Long teamId);
    long countByTeamIdAndStatus(Long teamId, ContentStatus status);
    long countByTeamIdAndCreatedById(Long teamId, Long userId);
    long countByTeamIdAndCreatedByIdAndStatus(Long teamId, Long userId, ContentStatus status);
    long countByCreatedByIdAndTeamIsNull(Long userId);
    long countByCreatedByIdAndTeamIsNullAndStatus(Long userId, ContentStatus status);


    @Query("SELECT FUNCTION('DAYNAME', c.scheduledDate) as dayName, COUNT(c.id) as totalCount " +
            "FROM Content c " +
            "WHERE c.scheduledDate >= FUNCTION('DATE', :startDate) AND c.scheduledDate <= FUNCTION('DATE', :endDate) " +
            "GROUP BY FUNCTION('DAYNAME', c.scheduledDate)")
    List<Object[]> countContentGroupedByDay(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT FUNCTION('DAYNAME', c.scheduledDate), COUNT(c) " +
            "FROM Content c " +
            "WHERE c.createdBy.id = :userId AND c.team IS NULL " +
            "AND c.scheduledDate >= FUNCTION('DATE', :startOfWeek) AND c.scheduledDate <= FUNCTION('DATE', :currentDate) " +
            "GROUP BY FUNCTION('DAYNAME', c.scheduledDate)")
    List<Object[]> countIndividualContentGroupedByDay(@Param("userId") Long userId, @Param("startOfWeek") java.time.LocalDateTime startOfWeek, @Param("currentDate") java.time.LocalDateTime currentDate);

    @Query("SELECT FUNCTION('DAYNAME', c.scheduledDate), COUNT(c) " +
            "FROM Content c " +
            "WHERE c.team.id = :teamId " +
            "AND c.scheduledDate >= FUNCTION('DATE', :startOfWeek) AND c.scheduledDate <= FUNCTION('DATE', :currentDate) " +
            "GROUP BY FUNCTION('DAYNAME', c.scheduledDate)")
    List<Object[]> countTeamContentGroupedByDay(@Param("teamId") Long teamId, @Param("startOfWeek") java.time.LocalDateTime startOfWeek, @Param("currentDate") java.time.LocalDateTime currentDate);

    @Query("SELECT FUNCTION('DAYNAME', c.scheduledDate), COUNT(c) " +
            "FROM Content c " +
            "WHERE c.team.id = :teamId AND c.createdBy.id = :userId " +
            "AND c.scheduledDate >= FUNCTION('DATE', :startOfWeek) AND c.scheduledDate <= FUNCTION('DATE', :currentDate) " +
            "GROUP BY FUNCTION('DAYNAME', c.scheduledDate)")
    List<Object[]> countUserTeamContentGroupedByDay(@Param("teamId") Long teamId, @Param("userId") Long userId, @Param("startOfWeek") java.time.LocalDateTime startOfWeek, @Param("currentDate") java.time.LocalDateTime currentDate);
}
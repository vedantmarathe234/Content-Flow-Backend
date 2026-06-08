package com.athenura.contentflow.content.repository;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.content.entity.Notification;
import com.athenura.contentflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndIsReadFalse(User user);
    List<Notification> findByUserAndIsReadFalse(User user);

    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.isRead = true
    WHERE n.contentId = :contentId
    """)
    void markAllByContentId(Long contentId);

    List<Notification>
    findByUserAndContentIdAndIsReadFalse(
            User user,
            Long contentId
    );

    List<Notification> findTop5ByOrderByCreatedAtDesc();

}

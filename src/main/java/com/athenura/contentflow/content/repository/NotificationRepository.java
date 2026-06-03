package com.athenura.contentflow.content.repository;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.content.entity.Notification;
import com.athenura.contentflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

}

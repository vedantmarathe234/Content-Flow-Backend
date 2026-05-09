package com.athenura.contentflow.repository;

import com.athenura.contentflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReopsitory extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

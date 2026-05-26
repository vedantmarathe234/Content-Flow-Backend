package com.athenura.contentflow.user.repository;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByDepartmentIdAndRole(Long departmentId, Role role);
    Optional<User> findByResetToken(
            String resetToken
    );
}

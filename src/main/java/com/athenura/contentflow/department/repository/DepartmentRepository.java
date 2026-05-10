package com.athenura.contentflow.department.repository;

import com.athenura.contentflow.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}

package com.athenura.contentflow.department.repository;

import com.athenura.contentflow.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    List<Department> findByNameContainingIgnoreCase(String name);

    @Query("SELECT d.name, COUNT(c.id) FROM Content c JOIN c.department d GROUP BY d.name ORDER BY COUNT(c.id) DESC")
    List<Object[]> getDepartmentContentCounts();
}

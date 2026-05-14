package com.athenura.contentflow.department.controller;

import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.department.service.DepartmentService;
import com.athenura.contentflow.department.dto.DepartmentRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public Department create(@RequestBody DepartmentRequestDTO dto) {
        return departmentService.createDepartment(dto);
    }

    @GetMapping("/all")
    public List<Department> getAll() {
        return departmentService.getAllDepartments();
    }
}

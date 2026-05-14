package com.athenura.contentflow.department.service;

import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.department.repository.DepartmentRepository;
import com.athenura.contentflow.department.dto.DepartmentRequestDTO;
import com.athenura.contentflow.user.repository.UserRepository; // योग्य इम्पोर्ट तपासा
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public Department createDepartment(DepartmentRequestDTO dto) {
        if(departmentRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Department already exists!");
        }

        Department dept = Department.builder()
                .name(dto.getName())
                .secretKey(dto.getSecretKey())
                .build();

        return departmentRepository.save(dept);
    }

    public List<Department> getAllDepartments(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if ("ADMIN".equals(user.getRole().name())) {
            return departmentRepository.findAll();
        }

        if (user.getDepartment() != null) {
            return List.of(user.getDepartment());
        }

        return List.of();
    }
}
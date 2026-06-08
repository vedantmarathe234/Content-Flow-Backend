package com.athenura.contentflow.department.controller;

import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.department.service.DepartmentService;
import com.athenura.contentflow.department.dto.DepartmentRequestDTO;
import com.athenura.contentflow.team.entity.Team;
import com.athenura.contentflow.team.repository.TeamRepository;
import com.athenura.contentflow.user.dto.UserResponseDTO;
import com.athenura.contentflow.user.repository.UserRepository;
import com.athenura.contentflow.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public Department create(@RequestBody DepartmentRequestDTO dto) {
        return departmentService.createDepartment(dto);
    }

    @GetMapping("/all")
    public List<Department> getAll(java.security.Principal principal) {
        return departmentService.getAllDepartments(principal.getName());
    }

    @GetMapping("/{id}/details")
    public Map<String, Object> getDepartmentDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        List<UserResponseDTO> interns = userRepository.findByDepartmentId(id).stream()
                .map(user -> {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
                    return dto;
                }).collect(Collectors.toList());

        List<Team> teams = teamRepository.findByDepartmentId(id);
        List<Map<String, Object>> teamResponse = teams.stream().map(t -> {
            Map<String, Object> teamMap = new HashMap<>();
            teamMap.put("id", t.getId());
            teamMap.put("name", t.getName());
            return teamMap;
        }).collect(Collectors.toList());

        response.put("departmentName", dept.getName());
        response.put("interns", interns);
        response.put("teams", teamResponse);

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public Department update(@PathVariable Long id, @RequestBody DepartmentRequestDTO dto) {
        return departmentService.updateDepartment(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }

    @GetMapping("/public")
    public List<Department> getPublicDepartments() {

        return departmentRepository.findAll();

    }
}
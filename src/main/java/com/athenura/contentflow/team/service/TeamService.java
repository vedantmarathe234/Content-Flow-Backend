package com.athenura.contentflow.team.service;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.department.repository.DepartmentRepository;
import com.athenura.contentflow.exception.ResourceNotFoundException;
import com.athenura.contentflow.team.dto.CreateTeamRequest;
import com.athenura.contentflow.team.dto.TeamResponse;
import com.athenura.contentflow.team.dto.UpdateTeamRequest;
import com.athenura.contentflow.team.entity.Team;
import com.athenura.contentflow.team.repository.TeamRepository;
import com.athenura.contentflow.user.dto.UserResponseDTO;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public List<UserResponseDTO> getAvailableInternsByDepartment(Long departmentId) {
        return userRepository.findByDepartmentIdAndRole(departmentId, Role.INTERN).stream()
                .map(user -> {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole().name());
                    dto.setTeamLeader(user.isTeamLeader());
                    if (user.getDepartment() != null) {
                        dto.setDepartmentName(user.getDepartment().getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        if (teamRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Team name already exists!");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        User leader = userRepository.findById(request.getTeamLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Leader not found"));

        if (leader.getDepartment() == null || !leader.getDepartment().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Leader must be from the same department!");
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setDepartment(department);
        team.setTeamLeader(leader);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            for (User member : members) {
                if (member.getDepartment() == null || !member.getDepartment().getId().equals(department.getId())) {
                    throw new IllegalArgumentException("Member " + member.getName() + " is not from the same department!");
                }
            }
            team.setMembers(members);
        }

        return mapToResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (request.getName() != null) {
            team.setName(request.getName());
        }

        User newLeader = userRepository.findById(request.getTeamLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("New Leader not found"));

        if (newLeader.getDepartment() == null || team.getDepartment() == null ||
                !newLeader.getDepartment().getId().equals(team.getDepartment().getId())) {
            throw new IllegalArgumentException("Leader must be from the same department!");
        }

        team.setTeamLeader(newLeader);

        if (request.getMemberIds() != null) {
            List<User> newMembers = userRepository.findAllById(request.getMemberIds());
            for (User member : newMembers) {
                if (member.getDepartment() == null || team.getDepartment() == null ||
                        !member.getDepartment().getId().equals(team.getDepartment().getId())) {
                    throw new IllegalArgumentException("Member " + member.getName() + " is not from the same department!");
                }
            }
            team.setMembers(newMembers);
        }

        return mapToResponse(teamRepository.save(team));
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found");
        }
        teamRepository.deleteById(teamId);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAllWithMembers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public TeamResponse getTeamByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Team> teams = teamRepository.findByMembersContaining(user);
        if (teams == null || teams.isEmpty()) {
            throw new ResourceNotFoundException("You are not assigned to any team!");
        }
        return mapToResponse(teams.get(0));
    }

    private TeamResponse mapToResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());

        if (team.getDepartment() != null) {
            response.setDepartmentId(team.getDepartment().getId());
            response.setDepartmentName(team.getDepartment().getName());
        }

        if (team.getTeamLeader() != null) {
            response.setTeamLeaderId(team.getTeamLeader().getId());
            response.setTeamLeaderName(team.getTeamLeader().getName());
        }

        if (team.getMembers() != null) {
            response.setMemberNames(team.getMembers().stream()
                    .map(User::getName)
                    .collect(Collectors.toList()));

            response.setMemberIds(team.getMembers().stream()
                    .map(User::getId)
                    .collect(Collectors.toList()));
        } else {
            response.setMemberNames(new ArrayList<>());
            response.setMemberIds(new ArrayList<>());
        }

        return response;
    }
}
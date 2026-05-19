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
                .orElseThrow(() -> new ResourceNotFoundException("Selected Leader user not found"));

        Team team = new Team();
        team.setName(request.getName());
        team.setDepartment(department);
        team.setTeamLeader(leader);
        Team savedTeam = teamRepository.save(team);


        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            for (User member : members) {
                member.setTeam(savedTeam);
            }
            userRepository.saveAll(members);
            savedTeam.setMembers(members);
        }


        leader.setRole(Role.TEAM_LEADER);
        leader.setTeam(savedTeam);
        userRepository.save(leader);

        return mapToResponse(savedTeam);
    }


    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));


        if (team.getMembers() != null) {
            for (User member : team.getMembers()) {
                member.setTeam(null);
            }
            userRepository.saveAll(team.getMembers());
        }


        User oldLeader = team.getTeamLeader();
        if (oldLeader != null) {
            oldLeader.setRole(Role.INTERN);
            oldLeader.setTeam(null);
            userRepository.save(oldLeader);
        }


        if (request.getName() != null) {
            team.setName(request.getName());
        }


        User newLeader = userRepository.findById(request.getTeamLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("New Leader not found"));
        newLeader.setRole(Role.TEAM_LEADER);
        newLeader.setTeam(team);
        userRepository.save(newLeader);
        team.setTeamLeader(newLeader);


        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> newMembers = userRepository.findAllById(request.getMemberIds());
            for (User member : newMembers) {
                member.setTeam(team);
            }
            userRepository.saveAll(newMembers);
            team.setMembers(newMembers);
        }

        Team updatedTeam = teamRepository.save(team);
        return mapToResponse(updatedTeam);
    }


    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (team.getMembers() != null) {
            for (User member : team.getMembers()) {
                member.setTeam(null);
            }
            userRepository.saveAll(team.getMembers());
        }

        User leader = team.getTeamLeader();
        if (leader != null) {
            leader.setRole(Role.INTERN);
            leader.setTeam(null);
            userRepository.save(leader);
        }

        teamRepository.delete(team);
    }


    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public TeamResponse getTeamByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getTeam() == null) {
            throw new ResourceNotFoundException("You are not assigned to any team yet!");
        }

        return mapToResponse(user.getTeam());
    }

    private TeamResponse mapToResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());
        response.setDepartmentName(team.getDepartment().getName());

        if (team.getTeamLeader() != null) {
            response.setTeamLeaderId(team.getTeamLeader().getId());
            response.setTeamLeaderName(team.getTeamLeader().getName());
        }

        if (team.getMembers() != null) {
            List<String> memberNames = team.getMembers().stream()
                    .map(User::getName)
                    .collect(Collectors.toList());
            response.setMemberNames(memberNames);
        }

        return response;
    }
}
package com.athenura.contentflow.team.controller;

import com.athenura.contentflow.team.dto.CreateTeamRequest;
import com.athenura.contentflow.team.dto.TeamResponse;
import com.athenura.contentflow.team.dto.UpdateTeamRequest;
import com.athenura.contentflow.team.service.TeamService;
import com.athenura.contentflow.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/department/{departmentId}/interns")
    public List<UserResponseDTO> getInternsByDepartment(@PathVariable Long departmentId) {
        return teamService.getAvailableInternsByDepartment(departmentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public TeamResponse createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{teamId}/edit")
    public TeamResponse updateTeam(@PathVariable Long teamId, @RequestBody UpdateTeamRequest request) {
        return teamService.updateTeam(teamId, request);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{teamId}")
    public String deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return "Team deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<TeamResponse> getAllTeams() {
        return teamService.getAllTeams();
    }


    @GetMapping("/my-team")
    public TeamResponse getTeamByInternId(Principal principal) {
        return teamService.getTeamByUserEmail(principal.getName());
    }
}
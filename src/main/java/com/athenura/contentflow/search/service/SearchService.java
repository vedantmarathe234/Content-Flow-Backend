package com.athenura.contentflow.search.service;

import com.athenura.contentflow.department.repository.DepartmentRepository;
import com.athenura.contentflow.search.dto.SearchResponse;
import com.athenura.contentflow.team.repository.TeamRepository;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;

    public SearchResponse search(String query) {

        SearchResponse response = new SearchResponse();

        response.setUsers(
                userRepository.findByNameContainingIgnoreCase(query)
        );

        response.setTeams(
                teamRepository.findByNameContainingIgnoreCase(query)
        );

        response.setDepartments(
                departmentRepository.findByNameContainingIgnoreCase(query)
        );

        return response;
    }
}
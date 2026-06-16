package com.athenura.contentflow.search.service;

import com.athenura.contentflow.department.repository.DepartmentRepository;
import com.athenura.contentflow.search.dto.SearchResponse;
import com.athenura.contentflow.search.dto.SearchResponse.SearchResultItem;
import com.athenura.contentflow.team.repository.TeamRepository;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;

    public SearchResponse search(String query) {
        SearchResponse response = new SearchResponse();

        response.setUsers(
                userRepository.findByNameContainingIgnoreCase(query).stream()
                        .map(u -> new SearchResultItem(u.getId(), u.getName()))
                        .collect(Collectors.toList())
        );

        response.setTeams(
                teamRepository.findByNameContainingIgnoreCase(query).stream()
                        .map(t -> new SearchResultItem(t.getId(), t.getName()))
                        .collect(Collectors.toList())
        );

        response.setDepartments(
                departmentRepository.findByNameContainingIgnoreCase(query).stream()
                        .map(d -> new SearchResultItem(d.getId(), d.getName()))
                        .collect(Collectors.toList())
        );

        return response;
    }
}
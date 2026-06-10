package com.athenura.contentflow.search.dto;

import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.team.entity.Team;
import com.athenura.contentflow.user.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {

    private List<User> users;
    private List<Team> teams;
    private List<Department> departments;
}

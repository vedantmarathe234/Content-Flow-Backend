package com.athenura.contentflow.team.entity;

import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;


    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;


    @ManyToOne
    @JoinColumn(name = "leader_id")
    private User teamLeader;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    public boolean isEmpty() {
        return this.name == null || this.name.trim().isEmpty();
    }
}

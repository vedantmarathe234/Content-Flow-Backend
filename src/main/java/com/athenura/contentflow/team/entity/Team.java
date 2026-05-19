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


    @OneToOne
    @JoinColumn(name = "leader_id")
    private User teamLeader;



    @OneToMany(mappedBy = "team", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<User> members = new ArrayList<>();
}

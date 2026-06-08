package com.athenura.contentflow.user.entity;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.department.entity.Department;
import com.athenura.contentflow.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String resetToken;

    @OneToMany(mappedBy = "teamLeader")
    private List<Team> teamsLed = new ArrayList<>();


    @ManyToMany(mappedBy = "members")
    private List<Team> teams = new ArrayList<>();

}
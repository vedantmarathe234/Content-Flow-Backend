package com.athenura.contentflow.team.repository;

import com.athenura.contentflow.team.entity.Team;
import com.athenura.contentflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByName(String name);


    List<Team> findByMembersContaining(User user);


    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members")
    List<Team> findAllWithMembers();
}
package com.sportstournament.repository;

import com.sportstournament.model.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByCity(String city);

    Page<Team> findByCity(String city, Pageable pageable);

    boolean existsByName(String name);

    List<Team> findByIdIn(List<Long> ids);

    List<Team> findAllById(Iterable<Long> ids);
}
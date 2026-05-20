package com.sportstournament.repository;

import com.sportstournament.model.entity.Tournament;
import com.sportstournament.model.enums.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(TournamentStatus status);

    // Додати цей метод для пагінації за статусом
    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    List<Tournament> findBySportType(String sportType);

    List<Tournament> findByStartDateAfter(LocalDate date);

    boolean existsByName(String name);
}
package com.sportstournament.repository;

import com.sportstournament.model.entity.Match;
import com.sportstournament.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // Виправлено: findByTournamentId → findByTournament_Id
    List<Match> findByTournament_Id(Long tournamentId);

    // Виправлено: findByTournamentIdAndRound → findByTournament_IdAndRound
    List<Match> findByTournament_IdAndRound(Long tournamentId, int round);

    List<Match> findByTournament_IdAndStatus(Long tournamentId, MatchStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Match m WHERE m.tournament.id = :tournamentId")
    void deleteByTournamentId(@Param("tournamentId") Long tournamentId);

    long countByTournament_IdAndStatus(Long tournamentId, MatchStatus status);
}
package com.sportstournament.controller;

import com.sportstournament.model.dto.StandingResponseDTO;
import com.sportstournament.model.dto.TournamentRequestDTO;
import com.sportstournament.model.entity.Tournament;
import com.sportstournament.model.enums.TournamentStatus;
import com.sportstournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    // GET з пагінацією, фільтрацією та сортуванням
    @GetMapping
    public ResponseEntity<Page<Tournament>> getAllTournaments(
            @RequestParam(required = false) TournamentStatus status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Tournament> tournaments = tournamentService.getAllTournaments(status, pageable);
        return ResponseEntity.ok(tournaments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable Long id) {
        Tournament tournament = tournamentService.getTournamentById(id);
        return ResponseEntity.ok(tournament);
    }

    @PostMapping
    public ResponseEntity<Tournament> createTournament(@Valid @RequestBody TournamentRequestDTO request) {
        Tournament tournament = tournamentService.createTournament(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tournament> updateTournament(@PathVariable Long id,
                                                       @Valid @RequestBody TournamentRequestDTO request) {
        Tournament tournament = tournamentService.updateTournament(id, request);
        return ResponseEntity.ok(tournament);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/teams/{teamId}")
    public ResponseEntity<Void> addTeamToTournament(@PathVariable Long id, @PathVariable Long teamId) {
        tournamentService.addTeamToTournament(id, teamId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    public ResponseEntity<Void> removeTeamFromTournament(@PathVariable Long id, @PathVariable Long teamId) {
        tournamentService.removeTeamFromTournament(id, teamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/generate-bracket")
    public ResponseEntity<Void> generateBracket(@PathVariable Long id) {
        tournamentService.generateBracket(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/standings")
    public ResponseEntity<StandingResponseDTO> getStandings(@PathVariable Long id) {
        StandingResponseDTO standings = tournamentService.getStandings(id);
        return ResponseEntity.ok(standings);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<Void> finishTournament(@PathVariable Long id) {
        tournamentService.finishTournament(id);
        return ResponseEntity.ok().build();
    }
}
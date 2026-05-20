package com.sportstournament.controller;

import com.sportstournament.model.dto.StandingResponseDTO;
import com.sportstournament.model.dto.TournamentRequestDTO;
import com.sportstournament.model.entity.Tournament;
import com.sportstournament.service.TournamentService;
import jakarta.validation.Valid;
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

    @GetMapping
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        List<Tournament> tournaments = tournamentService.getAllTournaments();
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

    @PostMapping("/{id}/teams/{teamId}")
    public ResponseEntity<Void> addTeamToTournament(@PathVariable Long id, @PathVariable Long teamId) {
        tournamentService.addTeamToTournament(id, teamId);
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
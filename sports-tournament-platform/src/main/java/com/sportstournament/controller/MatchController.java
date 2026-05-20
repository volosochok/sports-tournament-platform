package com.sportstournament.controller;

import com.sportstournament.model.dto.MatchResultDTO;
import com.sportstournament.model.entity.Match;
import com.sportstournament.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        List<Match> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        Match match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<Match>> getMatchesByTournament(@PathVariable Long tournamentId) {
        List<Match> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/{id}/result")
    public ResponseEntity<Void> recordMatchResult(@PathVariable Long id,
                                                  @Valid @RequestBody MatchResultDTO result) {
        matchService.recordMatchResult(id, result);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Match> updateMatch(@PathVariable Long id,
                                             @Valid @RequestBody MatchResultDTO result) {
        System.out.println("=== PUT updateMatch called for id: " + id);
        Match match = matchService.updateMatch(id, result);
        return ResponseEntity.ok(match);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        System.out.println("=== DELETE deleteMatch called for id: " + id);
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}
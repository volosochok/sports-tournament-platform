package com.sportstournament.controller;

import com.sportstournament.repository.TeamRepository;
import com.sportstournament.repository.TournamentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;

    public TestController(TournamentRepository tournamentRepository,
                          TeamRepository teamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> status = new HashMap<>();

        try {
            long tournamentCount = tournamentRepository.count();
            long teamCount = teamRepository.count();

            status.put("status", "connected");
            status.put("database", "PostgreSQL");
            status.put("tournament_count", tournamentCount);
            status.put("team_count", teamCount);
            status.put("message", "Database connection successful!");

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("status", "error");
            status.put("message", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
}
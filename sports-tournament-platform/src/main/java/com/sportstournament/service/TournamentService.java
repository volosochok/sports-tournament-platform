package com.sportstournament.service;

import com.sportstournament.exception.ResourceNotFoundException;
import com.sportstournament.model.dto.StandingResponseDTO;
import com.sportstournament.model.dto.TeamStandingDTO;
import com.sportstournament.model.dto.TournamentRequestDTO;
import com.sportstournament.model.entity.Match;
import com.sportstournament.model.entity.Team;
import com.sportstournament.model.entity.Tournament;
import com.sportstournament.model.enums.TournamentStatus;
import com.sportstournament.repository.MatchRepository;
import com.sportstournament.repository.TeamRepository;
import com.sportstournament.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final BracketService bracketService;
    private final ScoringService scoringService;

    public TournamentService(TournamentRepository tournamentRepository,
                             TeamRepository teamRepository,
                             MatchRepository matchRepository,
                             BracketService bracketService,
                             ScoringService scoringService) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.bracketService = bracketService;
        this.scoringService = scoringService;
    }

    public List<Tournament> getAllTournaments() {
        System.out.println("=== getAllTournaments called");
        List<Tournament> tournaments = tournamentRepository.findAll();
        System.out.println("Found " + tournaments.size() + " tournaments");
        return tournaments;
    }

    public Tournament getTournamentById(Long id) {
        System.out.println("=== getTournamentById called for id: " + id);
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
        System.out.println("Found tournament: " + tournament.getName());
        return tournament;
    }

    public Tournament createTournament(TournamentRequestDTO request) {
        System.out.println("=== createTournament called for: " + request.getName());
        Tournament tournament = new Tournament(
                request.getName(),
                request.getSportType(),
                request.getStartDate(),
                request.getEndDate()
        );
        Tournament saved = tournamentRepository.save(tournament);
        System.out.println("Created tournament with id: " + saved.getId());
        return saved;
    }

    @Transactional
    public void addTeamToTournament(Long tournamentId, Long teamId) {
        System.out.println("=== addTeamToTournament called: tournamentId=" + tournamentId + ", teamId=" + teamId);

        Tournament tournament = getTournamentById(tournamentId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (tournament.getStatus() != TournamentStatus.PENDING) {
            throw new IllegalStateException("Cannot add teams to tournament that is not in PENDING status. Current status: " + tournament.getStatus());
        }

        if (!tournament.getTeamIds().contains(teamId)) {
            tournament.getTeamIds().add(teamId);
            tournamentRepository.save(tournament);
            System.out.println("Team " + teamId + " added to tournament " + tournamentId);
        } else {
            System.out.println("Team " + teamId + " already in tournament " + tournamentId);
        }
    }

    @Transactional
    public void generateBracket(Long tournamentId) {
        System.out.println("=== generateBracket called for tournamentId: " + tournamentId);

        Tournament tournament = getTournamentById(tournamentId);
        System.out.println("Tournament status: " + tournament.getStatus());
        System.out.println("Teams in tournament: " + tournament.getTeamIds());

        if (tournament.getStatus() != TournamentStatus.PENDING) {
            throw new IllegalStateException("Bracket can only be generated for PENDING tournament. Current status: " + tournament.getStatus());
        }

        if (tournament.getTeamIds() == null || tournament.getTeamIds().size() < 2) {
            throw new IllegalStateException("At least 2 teams are required to generate bracket. Current team count: " +
                    (tournament.getTeamIds() == null ? 0 : tournament.getTeamIds().size()));
        }

        // Delete existing bracket if any
        matchRepository.deleteByTournamentId(tournamentId);
        tournament.getBracket().clear();

        // Get teams and generate first round
        List<Team> teams = teamRepository.findAllByIds(tournament.getTeamIds());
        System.out.println("Retrieved " + teams.size() + " teams from repository");

        int totalRounds = bracketService.calculateTotalRounds(teams.size());
        System.out.println("Tournament will have " + totalRounds + " rounds (including final)");

        List<Match> firstRound = bracketService.generateFirstRound(tournamentId, teams);
        System.out.println("Generated " + firstRound.size() + " first round matches");

        // Save matches
        for (Match match : firstRound) {
            matchRepository.save(match);
            tournament.getBracket().add(match);
        }

        tournament.setStatus(TournamentStatus.ONGOING);
        tournamentRepository.save(tournament);
        System.out.println("Tournament " + tournamentId + " status changed to ONGOING");
    }

    public StandingResponseDTO getStandings(Long tournamentId) {
        System.out.println("=== getStandings called for tournamentId: " + tournamentId);

        try {
            // 1. Get tournament
            Tournament tournament = getTournamentById(tournamentId);
            System.out.println("Tournament name: " + tournament.getName());
            System.out.println("Tournament status: " + tournament.getStatus());

            // 2. Get teams
            List<Long> teamIds = tournament.getTeamIds();
            System.out.println("Team IDs from tournament: " + teamIds);

            List<Team> teams;
            if (teamIds == null || teamIds.isEmpty()) {
                System.out.println("No teams found in tournament");
                teams = new ArrayList<>();
            } else {
                teams = teamRepository.findAllByIds(teamIds);
                System.out.println("Teams retrieved from repository: " + teams.size());
                for (Team team : teams) {
                    System.out.println("  - Team ID: " + team.getId() + ", Name: " + team.getName() + ", Points: " + team.getPoints());
                }
            }

            // 3. Get matches
            List<Match> matches = matchRepository.findByTournamentId(tournamentId);
            System.out.println("Matches retrieved: " + matches.size());
            for (Match match : matches) {
                System.out.println("  - Match ID: " + match.getId() +
                        ", Team1: " + match.getTeam1Id() +
                        ", Team2: " + match.getTeam2Id() +
                        ", Score: " + match.getTeam1Score() + "-" + match.getTeam2Score() +
                        ", Status: " + match.getStatus());
            }

            // 4. Calculate standings
            List<TeamStandingDTO> standings = scoringService.calculateStandings(teams, matches);
            System.out.println("Standings calculated, size: " + standings.size());

            // 5. Sort standings
            if (!standings.isEmpty()) {
                List<TeamStandingDTO> mutableStandings = new ArrayList<>(standings);
                mutableStandings.sort((a, b) -> {
                    if (a.getPoints() != b.getPoints()) {
                        return Integer.compare(b.getPoints(), a.getPoints());
                    }
                    if (a.getGoalDifference() != b.getGoalDifference()) {
                        return Integer.compare(b.getGoalDifference(), a.getGoalDifference());
                    }
                    return Integer.compare(b.getGoalsScored(), a.getGoalsScored());
                });

                // Assign ranks
                for (int i = 0; i < mutableStandings.size(); i++) {
                    mutableStandings.get(i).setRank(i + 1);
                }

                // Update standings reference
                standings = mutableStandings;
            }

            // 6. Determine winner
            TeamStandingDTO winner = null;
            if (tournament.getStatus() == TournamentStatus.FINISHED && !standings.isEmpty()) {
                winner = standings.get(0);
                System.out.println("Winner: " + winner.getTeamName() + " with " + winner.getPoints() + " points");
            }

            StandingResponseDTO response = new StandingResponseDTO(tournamentId, tournament.getName(), standings, winner);
            System.out.println("=== getStandings completed successfully");
            return response;

        } catch (Exception e) {
            System.err.println("=== ERROR in getStandings ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void finishTournament(Long tournamentId) {
        System.out.println("=== finishTournament called for tournamentId: " + tournamentId);

        Tournament tournament = getTournamentById(tournamentId);

        if (tournament.getStatus() != TournamentStatus.ONGOING) {
            throw new IllegalStateException("Only ONGOING tournament can be finished. Current status: " + tournament.getStatus());
        }

        tournament.setStatus(TournamentStatus.FINISHED);
        tournamentRepository.save(tournament);
        System.out.println("Tournament " + tournamentId + " finished");
    }
}
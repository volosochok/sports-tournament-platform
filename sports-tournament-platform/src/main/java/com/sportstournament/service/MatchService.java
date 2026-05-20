package com.sportstournament.service;

import com.sportstournament.exception.ResourceNotFoundException;
import com.sportstournament.model.dto.MatchResultDTO;
import com.sportstournament.model.entity.Match;
import com.sportstournament.model.entity.Team;
import com.sportstournament.model.entity.Tournament;
import com.sportstournament.model.enums.MatchStatus;
import com.sportstournament.model.enums.TournamentStatus;
import com.sportstournament.repository.MatchRepository;
import com.sportstournament.repository.TeamRepository;
import com.sportstournament.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final BracketService bracketService;

    public MatchService(MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        TournamentRepository tournamentRepository,
                        BracketService bracketService) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.bracketService = bracketService;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public List<Match> getMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    public List<Match> getMatchesByTournamentAndRound(Long tournamentId, int round) {
        return matchRepository.findByTournamentIdAndRound(tournamentId, round);
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    @Transactional
    public void recordMatchResult(Long matchId, MatchResultDTO result) {
        Match match = getMatchById(matchId);

        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Match is already finished");
        }

        Tournament tournament = tournamentRepository.findById(match.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.ONGOING) {
            throw new IllegalStateException("Cannot record result for tournament that is not ONGOING");
        }

        // Update match with result
        match.setTeam1Score(result.getTeam1Score());
        match.setTeam2Score(result.getTeam2Score());
        match.setStatus(MatchStatus.FINISHED);

        // Determine winner
        if (result.getTeam1Score() > result.getTeam2Score()) {
            match.setWinnerId(match.getTeam1Id());
        } else if (result.getTeam2Score() > result.getTeam1Score()) {
            match.setWinnerId(match.getTeam2Id());
        }
        // If draw, in knockout tournament this shouldn't happen
        // For simplicity, if draw, team1 wins

        matchRepository.save(match);

        System.out.println("Match " + matchId + " recorded. Winner: " + match.getWinnerId());

        // Check if current round is complete and generate next round
        checkAndGenerateNextRound(tournament, match.getRound());

        // Check if tournament is finished
        checkAndFinishTournament(tournament);
    }

    private void checkAndGenerateNextRound(Tournament tournament, int completedRound) {
        // Get all matches of the completed round
        List<Match> currentRoundMatches = matchRepository.findByTournamentIdAndRound(tournament.getId(), completedRound);

        // Check if all matches in this round are finished
        boolean allFinished = currentRoundMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.FINISHED);

        if (!allFinished) {
            System.out.println("Round " + completedRound + " not complete yet. Waiting for other matches...");
            return;
        }

        // Get winners
        List<Long> winners = currentRoundMatches.stream()
                .filter(m -> m.getWinnerId() != null)
                .map(Match::getWinnerId)
                .toList();

        System.out.println("Round " + completedRound + " complete. Winners: " + winners);

        // If only one winner, tournament is over
        if (winners.size() <= 1) {
            tournament.setStatus(TournamentStatus.FINISHED);
            tournamentRepository.save(tournament);
            System.out.println("Tournament " + tournament.getId() + " finished!");
            return;
        }

        // Generate next round matches
        List<Match> nextRoundMatches = bracketService.generateNextRound(tournament.getId(), currentRoundMatches, completedRound);

        if (nextRoundMatches != null && !nextRoundMatches.isEmpty()) {
            for (Match match : nextRoundMatches) {
                // Check if this match already exists
                boolean exists = matchRepository.findByTournamentIdAndRound(tournament.getId(), match.getRound())
                        .stream().anyMatch(m ->
                                (m.getTeam1Id().equals(match.getTeam1Id()) && m.getTeam2Id().equals(match.getTeam2Id())) ||
                                        (m.getTeam1Id().equals(match.getTeam2Id()) && m.getTeam2Id().equals(match.getTeam1Id()))
                        );

                if (!exists) {
                    matchRepository.save(match);
                    tournament.getBracket().add(match);
                    System.out.println("Created match for round " + match.getRound() + ": Team " +
                            match.getTeam1Id() + " vs Team " + match.getTeam2Id());
                }
            }
            tournamentRepository.save(tournament);

            // CRITICAL FIX: After generating next round, check if we need to continue
            // If next round has only 1 match (final) and both teams exist, we're good
            // But if next round creates a match that needs to be played, we wait for results
            System.out.println("Next round (" + (completedRound + 1) + ") created with " + nextRoundMatches.size() + " matches");
        } else if (nextRoundMatches == null && winners.size() > 1) {
            // If generateNextRound returned null but we have winners, something went wrong
            // Try to generate manually
            System.out.println("WARNING: generateNextRound returned null but we have " + winners.size() + " winners");
            List<Match> manualMatches = new ArrayList<>();
            int nextRound = completedRound + 1;
            for (int i = 0; i < winners.size(); i += 2) {
                if (i + 1 < winners.size()) {
                    Match match = new Match(nextRound, winners.get(i), winners.get(i + 1), tournament.getId());
                    matchRepository.save(match);
                    tournament.getBracket().add(match);
                    manualMatches.add(match);
                    System.out.println("Manually created match: Team " + winners.get(i) + " vs Team " + winners.get(i + 1));
                }
            }
            if (!manualMatches.isEmpty()) {
                tournamentRepository.save(tournament);
            }
        }
    }

    private void checkAndFinishTournament(Tournament tournament) {
        // Get the highest round number
        List<Match> allMatches = matchRepository.findByTournamentId(tournament.getId());
        int maxRound = allMatches.stream()
                .mapToInt(Match::getRound)
                .max()
                .orElse(0);

        // Check if all matches in the highest round are finished
        List<Match> finalRoundMatches = matchRepository.findByTournamentIdAndRound(tournament.getId(), maxRound);
        boolean allFinished = finalRoundMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.FINISHED);

        if (allFinished && tournament.getStatus() == TournamentStatus.ONGOING) {
            tournament.setStatus(TournamentStatus.FINISHED);
            tournamentRepository.save(tournament);

            // Determine champion
            if (!finalRoundMatches.isEmpty()) {
                Match finalMatch = finalRoundMatches.get(0);
                if (finalMatch.getWinnerId() != null) {
                    Team champion = teamRepository.findById(finalMatch.getWinnerId()).orElse(null);
                    System.out.println("🏆 CHAMPION: " + (champion != null ? champion.getName() : "Team " + finalMatch.getWinnerId()));
                }
            }
        }
    }
}
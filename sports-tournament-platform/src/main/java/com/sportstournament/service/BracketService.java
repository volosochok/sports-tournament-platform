package com.sportstournament.service;

import com.sportstournament.model.entity.Match;
import com.sportstournament.model.entity.Team;
import com.sportstournament.model.enums.MatchStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BracketService {

    /**
     * Generates first round matches for knockout tournament
     * Handles byes (teams that automatically advance)
     */
    public List<Match> generateFirstRound(Long tournamentId, List<Team> teams) {
        List<Match> matches = new ArrayList<>();

        if (teams == null || teams.size() < 2) {
            return matches;
        }

        // Shuffle teams for random seeding
        List<Team> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams);

        int teamCount = shuffledTeams.size();
        int optimalSize = nextPowerOfTwo(teamCount);
        int byes = optimalSize - teamCount;

        System.out.println("Tournament setup: " + teamCount + " teams, " + byes + " byes");

        // Calculate how many matches in first round
        int matchesInFirstRound = teamCount / 2;

        // If odd number of teams, some get byes
        List<Long> byeTeams = new ArrayList<>();
        List<Team> teamsToPlay = new ArrayList<>(shuffledTeams);

        // Give byes to random teams
        if (byes > 0) {
            Collections.shuffle(teamsToPlay);
            for (int i = 0; i < byes; i++) {
                byeTeams.add(teamsToPlay.remove(0).getId());
            }
            System.out.println("Bye teams (automatically advance to round 2): " + byeTeams);
        }

        // Create matches for teams that actually play
        int round = 1;
        for (int i = 0; i < teamsToPlay.size(); i += 2) {
            if (i + 1 < teamsToPlay.size()) {
                Match match = new Match(
                        round,
                        teamsToPlay.get(i).getId(),
                        teamsToPlay.get(i + 1).getId(),
                        tournamentId
                );
                matches.add(match);
                System.out.println("Created match: Team " + teamsToPlay.get(i).getId() + " vs Team " + teamsToPlay.get(i + 1).getId());
            }
        }

        // Create placeholder matches for bye teams (they auto-win)
        for (Long byeTeamId : byeTeams) {
            Match byeMatch = new Match(round, byeTeamId, null, tournamentId);
            byeMatch.setStatus(MatchStatus.FINISHED);
            byeMatch.setWinnerId(byeTeamId);
            byeMatch.setTeam1Score(1);
            byeMatch.setTeam2Score(0);
            matches.add(byeMatch);
            System.out.println("Bye match created for team: " + byeTeamId);
        }

        System.out.println("Generated " + matches.size() + " first round matches (" +
                (matches.size() - byeTeams.size()) + " real matches, " + byeTeams.size() + " byes)");

        return matches;
    }

    /**
     * Generates next round matches based on winners of current round
     */
    public List<Match> generateNextRound(Long tournamentId, List<Match> currentRoundMatches, int currentRound) {
        List<Match> nextRoundMatches = new ArrayList<>();

        // Get winners from current round
        List<Long> winners = new ArrayList<>();
        for (Match match : currentRoundMatches) {
            if (match.getStatus() == MatchStatus.FINISHED && match.getWinnerId() != null) {
                winners.add(match.getWinnerId());
            } else {
                // Not all matches finished yet
                System.out.println("Round " + currentRound + " not finished yet - waiting for all matches to complete");
                return null;
            }
        }

        System.out.println("Winners from round " + currentRound + ": " + winners);

        // If only one winner, tournament is over
        if (winners.size() <= 1) {
            System.out.println("Tournament finished - champion determined!");
            return nextRoundMatches;
        }

        // Create matches for next round
        int nextRound = currentRound + 1;
        for (int i = 0; i < winners.size(); i += 2) {
            if (i + 1 < winners.size()) {
                Match match = new Match(
                        nextRound,
                        winners.get(i),
                        winners.get(i + 1),
                        tournamentId
                );
                nextRoundMatches.add(match);
                System.out.println("Creating " + getRoundName(nextRound, 0) + " match: Team " + winners.get(i) + " vs Team " + winners.get(i + 1));
            } else if (winners.size() == 2) {
                // This is the final match - it should be created above
                System.out.println("Final match created");
            } else if (winners.size() % 2 != 0) {
                // Odd number of winners - last one gets a bye to next round
                System.out.println("Odd number of winners (" + winners.size() + ") - last team gets a bye");
                // Add the last winner as a "bye" for the next round
                // This team will wait for the winner of the previous match
            }
        }

        return nextRoundMatches;
    }

    /**
     * Get round name (Quarterfinal, Semifinal, Final, etc.)
     */
    public String getRoundName(int round, int totalRounds) {
        if (round == totalRounds) {
            return "FINAL";
        } else if (round == totalRounds - 1) {
            return "SEMIFINAL";
        } else if (round == totalRounds - 2) {
            return "QUARTERFINAL";
        } else if (round == totalRounds - 3) {
            return "ROUND_OF_16";
        } else {
            return "ROUND_" + round;
        }
    }

    /**
     * Calculate total number of rounds needed
     */
    public int calculateTotalRounds(int teamCount) {
        int optimalSize = nextPowerOfTwo(teamCount);
        return (int) (Math.log(optimalSize) / Math.log(2));
    }

    /**
     * Find next power of 2 greater than or equal to n
     */
    private int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power <<= 1;
        }
        return power;
    }
}
package com.sportstournament.service;

import com.sportstournament.model.dto.TeamStandingDTO;
import com.sportstournament.model.entity.Match;
import com.sportstournament.model.entity.Team;
import com.sportstournament.model.enums.MatchStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScoringService {

    public void updateTeamPoints(Team team1, Team team2, int score1, int score2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        if (score1 > score2) {
            team1.addPoints(3);
        } else if (score2 > score1) {
            team2.addPoints(3);
        } else {
            team1.addPoints(1);
            team2.addPoints(1);
        }
    }

    public List<TeamStandingDTO> calculateStandings(List<Team> teams, List<Match> matches) {
        Map<Long, TeamStats> statsMap = new HashMap<>();

        // Ініціалізація статистики для всіх команд
        for (Team team : teams) {
            if (team != null && team.getId() != null) {
                statsMap.put(team.getId(), new TeamStats(team.getId(), team.getName()));
            }
        }

        // Якщо немає команд — повертаємо порожній список
        if (statsMap.isEmpty()) {
            return new ArrayList<>();
        }

        // Обробка завершених матчів
        for (Match match : matches) {
            if (match == null || match.getStatus() != MatchStatus.FINISHED) {
                continue;
            }

            Long team1Id = match.getTeam1Id();
            Long team2Id = match.getTeam2Id();
            Integer score1 = match.getTeam1Score();
            Integer score2 = match.getTeam2Score();

            if (team1Id == null || team2Id == null || score1 == null || score2 == null) {
                continue; // пропускаємо неповний матч
            }

            TeamStats stats1 = statsMap.get(team1Id);
            TeamStats stats2 = statsMap.get(team2Id);
            if (stats1 == null || stats2 == null) {
                continue; // команда не належить турніру (не має бути, але про всяк випадок)
            }

            // Оновлення голів
            stats1.goalsScored += score1;
            stats1.goalsConceded += score2;
            stats2.goalsScored += score2;
            stats2.goalsConceded += score1;

            stats1.matchesPlayed++;
            stats2.matchesPlayed++;

            if (score1 > score2) {
                stats1.wins++;
                stats1.points += 3;
                stats2.losses++;
            } else if (score2 > score1) {
                stats2.wins++;
                stats2.points += 3;
                stats1.losses++;
            } else {
                stats1.draws++;
                stats1.points += 1;
                stats2.draws++;
                stats2.points += 1;
            }
        }

        // Конвертація в DTO
        return statsMap.values().stream()
                .map(TeamStats::toDTO)
                .toList();
    }

    private static class TeamStats {
        Long teamId;
        String teamName;
        int points = 0;
        int matchesPlayed = 0;
        int wins = 0;
        int draws = 0;
        int losses = 0;
        int goalsScored = 0;
        int goalsConceded = 0;

        TeamStats(Long teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName != null ? teamName : "Unknown";
        }

        TeamStandingDTO toDTO() {
            return new TeamStandingDTO(
                    teamId, teamName, points, matchesPlayed,
                    wins, draws, losses, goalsScored, goalsConceded, 0
            );
        }
    }
}
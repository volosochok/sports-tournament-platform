package com.sportstournament.model.dto;

public class TeamStandingDTO {
    private Long teamId;
    private String teamName;
    private int points;
    private int matchesPlayed;
    private int wins;
    private int draws;
    private int losses;
    private int goalsScored;
    private int goalsConceded;
    private int goalDifference;
    private int rank;

    // Constructors
    public TeamStandingDTO() {}

    public TeamStandingDTO(Long teamId, String teamName, int points, int matchesPlayed,
                           int wins, int draws, int losses, int goalsScored,
                           int goalsConceded, int rank) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.points = points;
        this.matchesPlayed = matchesPlayed;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsScored = goalsScored;
        this.goalsConceded = goalsConceded;
        this.goalDifference = goalsScored - goalsConceded;
        this.rank = rank;
    }

    // Getters and Setters
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getGoalsScored() { return goalsScored; }
    public void setGoalsScored(int goalsScored) { this.goalsScored = goalsScored; }

    public int getGoalsConceded() { return goalsConceded; }
    public void setGoalsConceded(int goalsConceded) { this.goalsConceded = goalsConceded; }

    public int getGoalDifference() { return goalDifference; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
package com.sportstournament.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MatchResultDTO {
    @NotNull(message = "Team 1 score is required")
    @Min(value = 0, message = "Score cannot be negative")
    private Integer team1Score;
    
    @NotNull(message = "Team 2 score is required")
    @Min(value = 0, message = "Score cannot be negative")
    private Integer team2Score;

    // Getters and Setters
    public Integer getTeam1Score() { return team1Score; }
    public void setTeam1Score(Integer team1Score) { this.team1Score = team1Score; }

    public Integer getTeam2Score() { return team2Score; }
    public void setTeam2Score(Integer team2Score) { this.team2Score = team2Score; }
}
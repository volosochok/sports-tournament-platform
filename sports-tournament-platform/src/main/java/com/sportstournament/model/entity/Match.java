package com.sportstournament.model.entity;

import com.sportstournament.model.enums.MatchStatus;

public class Match {
    private Long id;
    private int round;
    private Long team1Id;
    private Long team2Id;
    private Integer team1Score;
    private Integer team2Score;
    private MatchStatus status;
    private Long winnerId;
    private Long tournamentId;

    // Constructors
    public Match() {}

    public Match(int round, Long team1Id, Long team2Id, Long tournamentId) {
        this.round = round;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.tournamentId = tournamentId;
        this.status = MatchStatus.SCHEDULED;
        this.team1Score = null;
        this.team2Score = null;
        this.winnerId = null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public Long getTeam1Id() { return team1Id; }
    public void setTeam1Id(Long team1Id) { this.team1Id = team1Id; }

    public Long getTeam2Id() { return team2Id; }
    public void setTeam2Id(Long team2Id) { this.team2Id = team2Id; }

    public Integer getTeam1Score() { return team1Score; }
    public void setTeam1Score(Integer team1Score) { this.team1Score = team1Score; }

    public Integer getTeam2Score() { return team2Score; }
    public void setTeam2Score(Integer team2Score) { this.team2Score = team2Score; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }

    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }
}
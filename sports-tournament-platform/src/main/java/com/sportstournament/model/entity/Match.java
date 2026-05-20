package com.sportstournament.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sportstournament.model.enums.MatchStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int round;

    @Column(name = "team1_id")
    private Long team1Id;

    @Column(name = "team2_id")
    private Long team2Id;

    @Column(name = "team1_score")
    private Integer team1Score;

    @Column(name = "team2_score")
    private Integer team2Score;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Column(name = "winner_id")
    private Long winnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    @JsonIgnore
    private Tournament tournament;

    // Constructors
    public Match() {}

    public Match(int round, Long team1Id, Long team2Id, Tournament tournament) {
        this.round = round;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.tournament = tournament;
        this.status = MatchStatus.SCHEDULED;
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

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public Long getTournamentId() {
        return tournament != null ? tournament.getId() : null;
    }
}
package com.sportstournament.model.entity;

import com.sportstournament.model.enums.TournamentStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tournament {
    private Long id;
    private String name;
    private String sportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private TournamentStatus status;
    private List<Match> bracket = new ArrayList<>();
    private List<Long> teamIds = new ArrayList<>();

    // Constructors
    public Tournament() {}

    public Tournament(String name, String sportType, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.sportType = sportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = TournamentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public List<Match> getBracket() { return bracket; }
    public void setBracket(List<Match> bracket) { this.bracket = bracket; }

    public List<Long> getTeamIds() { return teamIds; }
    public void setTeamIds(List<Long> teamIds) { this.teamIds = teamIds; }
}
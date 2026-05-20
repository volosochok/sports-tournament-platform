package com.sportstournament.model.dto;

import java.util.List;

public class StandingResponseDTO {
    private Long tournamentId;
    private String tournamentName;
    private List<TeamStandingDTO> standings;
    private TeamStandingDTO winner;

    // Constructors
    public StandingResponseDTO() {}

    public StandingResponseDTO(Long tournamentId, String tournamentName, 
                                List<TeamStandingDTO> standings, TeamStandingDTO winner) {
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.standings = standings;
        this.winner = winner;
    }

    // Getters and Setters
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public List<TeamStandingDTO> getStandings() { return standings; }
    public void setStandings(List<TeamStandingDTO> standings) { this.standings = standings; }

    public TeamStandingDTO getWinner() { return winner; }
    public void setWinner(TeamStandingDTO winner) { this.winner = winner; }
}
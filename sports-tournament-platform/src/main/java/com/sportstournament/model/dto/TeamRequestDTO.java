package com.sportstournament.model.dto;

import jakarta.validation.constraints.NotBlank;

public class TeamRequestDTO {
    @NotBlank(message = "Team name is required")
    private String name;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String coachName;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }
}
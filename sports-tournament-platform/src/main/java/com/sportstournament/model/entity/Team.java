package com.sportstournament.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String city;

    @Column(name = "coach_name")
    private String coachName;

    private int points = 0;

    @JsonIgnore
    @ManyToMany(mappedBy = "teams")
    private List<Tournament> tournaments = new ArrayList<>();

    // Constructors
    public Team() {}

    public Team(String name, String city, String coachName) {
        this.name = name;
        this.city = city;
        this.coachName = coachName;
        this.points = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public void addPoints(int points) { this.points += points; }

    public List<Tournament> getTournaments() { return tournaments; }
    public void setTournaments(List<Tournament> tournaments) { this.tournaments = tournaments; }
}
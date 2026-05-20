package com.sportstournament.service;

import com.sportstournament.exception.DuplicateResourceException;
import com.sportstournament.exception.ResourceNotFoundException;
import com.sportstournament.model.dto.TeamRequestDTO;
import com.sportstournament.model.entity.Team;
import com.sportstournament.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

    public Team createTeam(TeamRequestDTO request) {
        // Check for duplicate name
        boolean exists = teamRepository.findAll().stream()
                .anyMatch(team -> team.getName().equalsIgnoreCase(request.getName()));
        
        if (exists) {
            throw new DuplicateResourceException("Team with name '" + request.getName() + "' already exists");
        }

        Team team = new Team(request.getName(), request.getCity(), request.getCoachName());
        return teamRepository.save(team);
    }
}
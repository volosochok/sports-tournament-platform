package com.sportstournament.service;

import com.sportstournament.exception.DuplicateResourceException;
import com.sportstournament.exception.ResourceNotFoundException;
import com.sportstournament.model.dto.TeamRequestDTO;
import com.sportstournament.model.entity.Team;
import com.sportstournament.model.enums.TournamentStatus;
import com.sportstournament.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Page<Team> getAllTeams(String city, Pageable pageable) {
        System.out.println("=== getAllTeams called with city: " + city + ", pageable: " + pageable);

        if (city != null && !city.isEmpty()) {
            return teamRepository.findByCity(city, pageable);
        }
        return teamRepository.findAll(pageable);
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

    @Transactional
    public Team updateTeam(Long id, TeamRequestDTO request) {
        System.out.println("=== updateTeam called for id: " + id);

        Team team = getTeamById(id);

        team.setName(request.getName());
        team.setCity(request.getCity());
        team.setCoachName(request.getCoachName());

        Team updated = teamRepository.save(team);
        System.out.println("Team " + id + " updated");
        return updated;
    }

    @Transactional
    public void deleteTeam(Long id) {
        System.out.println("=== deleteTeam called for id: " + id);

        Team team = getTeamById(id);

        // Перевіряємо, чи команда не бере участь у жодному активному турнірі
        boolean inActiveTournament = team.getTournaments().stream()
                .anyMatch(t -> t.getStatus() == TournamentStatus.PENDING || t.getStatus() == TournamentStatus.ONGOING);

        if (inActiveTournament) {
            throw new IllegalStateException("Cannot delete team that is participating in an active tournament");
        }

        teamRepository.delete(team);
        System.out.println("Team " + id + " deleted");
    }
}
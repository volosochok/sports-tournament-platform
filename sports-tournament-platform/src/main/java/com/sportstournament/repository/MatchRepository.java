package com.sportstournament.repository;

import com.sportstournament.model.entity.Match;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class MatchRepository {
    private final Map<Long, Match> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Match save(Match match) {
        if (match.getId() == null) {
            match.setId(idGenerator.getAndIncrement());
        }
        storage.put(match.getId(), match);
        return match;
    }

    public Optional<Match> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Match> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Match> findByTournamentId(Long tournamentId) {
        return storage.values().stream()
                .filter(match -> match.getTournamentId().equals(tournamentId))
                .collect(Collectors.toList());
    }

    public List<Match> findByTournamentIdAndRound(Long tournamentId, int round) {
        return storage.values().stream()
                .filter(match -> match.getTournamentId().equals(tournamentId) && match.getRound() == round)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    public void deleteByTournamentId(Long tournamentId) {
        storage.values().removeIf(match -> match.getTournamentId().equals(tournamentId));
    }
}
package com.sportstournament.repository;

import com.sportstournament.model.entity.Team;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TeamRepository {
    private final Map<Long, Team> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Team save(Team team) {
        if (team.getId() == null) {
            team.setId(idGenerator.getAndIncrement());
        }
        storage.put(team.getId(), team);
        return team;
    }

    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Team> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Team> findAllByIds(List<Long> ids) {
        return storage.entrySet().stream()
                .filter(entry -> ids.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
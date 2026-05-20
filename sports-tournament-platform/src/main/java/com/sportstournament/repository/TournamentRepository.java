package com.sportstournament.repository;

import com.sportstournament.model.entity.Tournament;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TournamentRepository {
    private final Map<Long, Tournament> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Tournament save(Tournament tournament) {
        if (tournament.getId() == null) {
            tournament.setId(idGenerator.getAndIncrement());
        }
        storage.put(tournament.getId(), tournament);
        return tournament;
    }

    public Optional<Tournament> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Tournament> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public void clear() {
        storage.clear();
    }
}
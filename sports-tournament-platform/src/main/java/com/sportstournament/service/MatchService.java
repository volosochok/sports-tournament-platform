package com.sportstournament.service;

import com.sportstournament.exception.ResourceNotFoundException;
import com.sportstournament.model.dto.MatchResultDTO;
import com.sportstournament.model.entity.Match;
import com.sportstournament.model.entity.Team;
import com.sportstournament.model.entity.Tournament;
import com.sportstournament.model.enums.MatchStatus;
import com.sportstournament.model.enums.TournamentStatus;
import com.sportstournament.repository.MatchRepository;
import com.sportstournament.repository.TeamRepository;
import com.sportstournament.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final BracketService bracketService;

    public MatchService(MatchRepository matchRepository,
                        TeamRepository teamRepository,
                        TournamentRepository tournamentRepository,
                        BracketService bracketService) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.bracketService = bracketService;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public List<Match> getMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournament_Id(tournamentId);
    }

    public List<Match> getMatchesByTournamentAndRound(Long tournamentId, int round) {
        return matchRepository.findByTournament_IdAndRound(tournamentId, round);
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    @Transactional
    public void recordMatchResult(Long matchId, MatchResultDTO result) {
        Match match = getMatchById(matchId);

        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Match is already finished");
        }

        // Отримуємо турнір через об'єкт Match (JPA)
        Tournament tournament = match.getTournament();
        if (tournament == null) {
            throw new ResourceNotFoundException("Tournament not found for match");
        }

        if (tournament.getStatus() != TournamentStatus.ONGOING) {
            throw new IllegalStateException("Cannot record result for tournament that is not ONGOING");
        }

        // Оновлюємо матч з результатом
        match.setTeam1Score(result.getTeam1Score());
        match.setTeam2Score(result.getTeam2Score());
        match.setStatus(MatchStatus.FINISHED);

        // Визначаємо переможця
        if (result.getTeam1Score() > result.getTeam2Score()) {
            match.setWinnerId(match.getTeam1Id());
        } else if (result.getTeam2Score() > result.getTeam1Score()) {
            match.setWinnerId(match.getTeam2Id());
        }
        // Якщо нічия - переможця немає

        matchRepository.save(match);

        System.out.println("Match " + matchId + " recorded. Winner: " + match.getWinnerId());

        // Перевіряємо поточний раунд та генеруємо наступний
        checkAndGenerateNextRound(tournament, match.getRound());

        // Перевіряємо, чи турнір завершено
        checkAndFinishTournament(tournament);
    }

    private void checkAndGenerateNextRound(Tournament tournament, int completedRound) {
        // Отримуємо всі матчі завершеного раунду
        List<Match> currentRoundMatches = matchRepository.findByTournament_IdAndRound(tournament.getId(), completedRound);

        // Перевіряємо, чи всі матчі раунду завершені
        boolean allFinished = currentRoundMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.FINISHED);

        if (!allFinished) {
            System.out.println("Round " + completedRound + " not complete yet. Waiting for other matches...");
            return;
        }

        // Отримуємо переможців
        List<Long> winners = currentRoundMatches.stream()
                .filter(m -> m.getWinnerId() != null)
                .map(Match::getWinnerId)
                .toList();

        System.out.println("Round " + completedRound + " complete. Winners: " + winners);

        // Якщо тільки один переможець - турнір завершено
        if (winners.size() <= 1) {
            tournament.setStatus(TournamentStatus.FINISHED);
            tournamentRepository.save(tournament);
            System.out.println("Tournament " + tournament.getId() + " finished!");
            return;
        }

        // Генеруємо матчі наступного раунду
        List<Match> nextRoundMatches = bracketService.generateNextRound(tournament, currentRoundMatches, completedRound);

        if (nextRoundMatches != null && !nextRoundMatches.isEmpty()) {
            for (Match match : nextRoundMatches) {
                // Встановлюємо зв'язок з турніром
                match.setTournament(tournament);

                // Перевіряємо, чи матч вже існує
                boolean exists = matchRepository.findByTournament_IdAndRound(tournament.getId(), match.getRound())
                        .stream().anyMatch(m ->
                                (m.getTeam1Id() != null && m.getTeam1Id().equals(match.getTeam1Id()) &&
                                        m.getTeam2Id() != null && m.getTeam2Id().equals(match.getTeam2Id())) ||
                                        (m.getTeam1Id() != null && m.getTeam1Id().equals(match.getTeam2Id()) &&
                                                m.getTeam2Id() != null && m.getTeam2Id().equals(match.getTeam1Id()))
                        );

                if (!exists) {
                    matchRepository.save(match);
                    tournament.getBracket().add(match);
                    System.out.println("Created match for round " + match.getRound() + ": Team " +
                            match.getTeam1Id() + " vs Team " + match.getTeam2Id());
                }
            }
            tournamentRepository.save(tournament);
            System.out.println("Next round (" + (completedRound + 1) + ") created with " + nextRoundMatches.size() + " matches");
        } else if (nextRoundMatches == null && winners.size() > 1) {
            // Якщо generateNextRound повернув null, але є переможці - створюємо вручну
            System.out.println("WARNING: generateNextRound returned null but we have " + winners.size() + " winners");
            List<Match> manualMatches = new ArrayList<>();
            int nextRound = completedRound + 1;
            for (int i = 0; i < winners.size(); i += 2) {
                if (i + 1 < winners.size()) {
                    Match match = new Match(nextRound, winners.get(i), winners.get(i + 1), tournament);
                    matchRepository.save(match);
                    tournament.getBracket().add(match);
                    manualMatches.add(match);
                    System.out.println("Manually created match: Team " + winners.get(i) + " vs Team " + winners.get(i + 1));
                }
            }
            if (!manualMatches.isEmpty()) {
                tournamentRepository.save(tournament);
            }
        }
    }

    private void checkAndFinishTournament(Tournament tournament) {
        // Отримуємо найвищий номер раунду
        List<Match> allMatches = matchRepository.findByTournament_Id(tournament.getId());
        int maxRound = allMatches.stream()
                .mapToInt(Match::getRound)
                .max()
                .orElse(0);

        // Перевіряємо, чи всі матчі найвищого раунду завершені
        List<Match> finalRoundMatches = matchRepository.findByTournament_IdAndRound(tournament.getId(), maxRound);
        boolean allFinished = finalRoundMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.FINISHED);

        if (allFinished && tournament.getStatus() == TournamentStatus.ONGOING) {
            tournament.setStatus(TournamentStatus.FINISHED);
            tournamentRepository.save(tournament);

            // Визначаємо чемпіона
            if (!finalRoundMatches.isEmpty()) {
                Match finalMatch = finalRoundMatches.get(0);
                if (finalMatch.getWinnerId() != null) {
                    Team champion = teamRepository.findById(finalMatch.getWinnerId()).orElse(null);
                    System.out.println("🏆 CHAMPION: " + (champion != null ? champion.getName() : "Team " + finalMatch.getWinnerId()));
                }
            }
        }
    }

    @Transactional
    public Match updateMatch(Long matchId, MatchResultDTO result) {
        System.out.println("=== updateMatch called for id: " + matchId);

        Match match = getMatchById(matchId);

        if (match.getStatus() == MatchStatus.FINISHED) {
            // Якщо матч вже завершено, потрібно спочатку відкотити бали
            throw new IllegalStateException("Cannot update finished match. Please contact administrator.");
        }

        // Оновлюємо результат
        match.setTeam1Score(result.getTeam1Score());
        match.setTeam2Score(result.getTeam2Score());
        match.setStatus(MatchStatus.FINISHED);

        if (result.getTeam1Score() > result.getTeam2Score()) {
            match.setWinnerId(match.getTeam1Id());
        } else if (result.getTeam2Score() > result.getTeam1Score()) {
            match.setWinnerId(match.getTeam2Id());
        }

        Match updated = matchRepository.save(match);
        System.out.println("Match " + matchId + " updated");
        return updated;
    }

    @Transactional
    public void deleteMatch(Long matchId) {
        System.out.println("=== deleteMatch called for id: " + matchId);

        Match match = getMatchById(matchId);
        Tournament tournament = match.getTournament();

        // Не можна видаляти матч, який вже зіграний
        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Cannot delete finished match");
        }

        // Якщо турнір вже завершено - не можна видаляти матчі
        if (tournament.getStatus() == TournamentStatus.FINISHED) {
            throw new IllegalStateException("Cannot delete match from finished tournament");
        }

        matchRepository.delete(match);
        tournament.getBracket().remove(match);
        tournamentRepository.save(tournament);
        System.out.println("Match " + matchId + " deleted");
    }
}
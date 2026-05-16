package com.stories.stories.services;

import com.stories.stories.models.*;
import com.stories.stories.repositories.TournamentMatchRepository;
import com.stories.stories.repositories.TournamentRepository;
import com.stories.stories.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMatchRepository matchRepository;
    private final UserRepository userRepository;

    public TournamentDetailResponse createTournament(TournamentCreateRequest request, User creator) {
        validateName(request.getName());
        validateSize(request.getSize());
        validateUsernameCount(request.getSize(), request.getUsernames());
        validateNoDuplicateUsernames(request.getUsernames());

        List<User> players = resolvePlayers(request.getUsernames());
        Collections.shuffle(players);

        Tournament tournament = new Tournament();
        tournament.setName(request.getName().trim());
        tournament.setSize(request.getSize());
        tournament.setStatus(TournamentStatus.ACTIVE);
        tournament.setCreatedAt(LocalDateTime.now());
        tournament.setCreator(creator);
        tournament.setPlayers(new ArrayList<>(players));

        List<TournamentMatch> matches = generateBracket(tournament, players);
        tournament.setMatches(matches);

        TournamentMatch activeMatch = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bracket must have an active match"));
        tournament.setActiveMatch(activeMatch);

        Tournament saved = tournamentRepository.save(tournament);
        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TournamentSummaryResponse> getTournaments() {
        return tournamentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TournamentDetailResponse getTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tournament not found: " + id));
        return toDetailResponse(tournament);
    }

    public void deleteTournament(Long id, User requester) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tournament not found: " + id));

        if (!tournament.getCreator().getId().equals(requester.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the tournament creator can perform this action");
        }

        tournamentRepository.delete(tournament);
    }

    public List<TournamentMatch> generateBracket(Tournament tournament, List<User> shuffledPlayers) {
        List<TournamentMatch> matches = new ArrayList<>();
        int playerIndex = 0;
        int size = tournament.getSize();

        if (size == 2) {
            TournamentMatch finalMatch = createMatch(tournament, MatchRound.FINAL, 0, null, MatchStatus.ACTIVE);
            finalMatch.setPlayer1(shuffledPlayers.get(playerIndex++));
            finalMatch.setPlayer2(shuffledPlayers.get(playerIndex++));
            matches.add(finalMatch);
        } else if (size == 4) {
            for (int i = 0; i < 2; i++) {
                TournamentMatch semi = createMatch(tournament, MatchRound.SEMI_FINAL, i, 0, MatchStatus.WAITING);
                semi.setPlayer1(shuffledPlayers.get(playerIndex++));
                semi.setPlayer2(shuffledPlayers.get(playerIndex++));
                matches.add(semi);
            }
            matches.add(createMatch(tournament, MatchRound.FINAL, 0, null, MatchStatus.WAITING));
            matches.get(0).setStatus(MatchStatus.ACTIVE);
        } else if (size == 8) {
            for (int i = 0; i < 4; i++) {
                int nextIdx = i / 2;
                TournamentMatch qf = createMatch(tournament, MatchRound.QUARTER_FINAL, i, nextIdx, MatchStatus.WAITING);
                qf.setPlayer1(shuffledPlayers.get(playerIndex++));
                qf.setPlayer2(shuffledPlayers.get(playerIndex++));
                matches.add(qf);
            }
            for (int i = 0; i < 2; i++) {
                matches.add(createMatch(tournament, MatchRound.SEMI_FINAL, i, 0, MatchStatus.WAITING));
            }
            matches.add(createMatch(tournament, MatchRound.FINAL, 0, null, MatchStatus.WAITING));
            matches.get(0).setStatus(MatchStatus.ACTIVE);
        }

        return matches;
    }

    public void advanceBracket(Tournament tournament, TournamentMatch completedMatch, User winner) {
        completedMatch.setStatus(MatchStatus.COMPLETED);
        completedMatch.setWinner(winner);
        matchRepository.save(completedMatch);

        if (completedMatch.getRound() == MatchRound.FINAL) {
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournament.setChampion(winner.getUserName());
            tournament.setActiveMatch(null);
            tournamentRepository.save(tournament);
            return;
        }

        MatchRound nextRound = nextRound(completedMatch.getRound());
        TournamentMatch nextMatch = matchRepository
                .findByTournamentAndRoundAndMatchIndex(
                        tournament, nextRound, completedMatch.getNextMatchIndex())
                .orElseThrow(() -> new IllegalStateException(
                        "Next match not found for round " + nextRound));

        if (completedMatch.getMatchIndex() % 2 == 0) {
            nextMatch.setPlayer1(winner);
        } else {
            nextMatch.setPlayer2(winner);
        }
        matchRepository.save(nextMatch);

        if (nextMatch.getPlayer1() != null && nextMatch.getPlayer2() != null) {
            nextMatch.setStatus(MatchStatus.ACTIVE);
            nextMatch.setScore1(0);
            nextMatch.setScore2(0);
            matchRepository.save(nextMatch);
            tournament.setActiveMatch(nextMatch);
            tournamentRepository.save(tournament);
        }
    }

    private TournamentMatch createMatch(
            Tournament tournament,
            MatchRound round,
            int matchIndex,
            Integer nextMatchIndex,
            MatchStatus status) {
        TournamentMatch match = new TournamentMatch();
        match.setTournament(tournament);
        match.setRound(round);
        match.setMatchIndex(matchIndex);
        match.setNextMatchIndex(nextMatchIndex);
        match.setStatus(status);
        match.setScore1(0);
        match.setScore2(0);
        return match;
    }

    private MatchRound nextRound(MatchRound round) {
        return switch (round) {
            case QUARTER_FINAL -> MatchRound.SEMI_FINAL;
            case SEMI_FINAL -> MatchRound.FINAL;
            default -> throw new IllegalArgumentException("No next round after FINAL");
        };
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tournament name must be between 3 and 100 characters");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3 || trimmed.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tournament name must be between 3 and 100 characters");
        }
    }

    private void validateSize(int size) {
        if (size != 2 && size != 4 && size != 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tournament size must be 2, 4, or 8");
        }
    }

    private void validateUsernameCount(int size, List<String> usernames) {
        if (usernames == null || usernames.size() != size) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Username count must equal tournament size");
        }
    }

    private void validateNoDuplicateUsernames(List<String> usernames) {
        Set<String> seen = new HashSet<>();
        for (String username : usernames) {
            String key = username == null ? "" : username.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Duplicate players are not allowed");
            }
        }
    }

    private List<User> resolvePlayers(List<String> usernames) {
        List<User> players = new ArrayList<>();
        for (String username : usernames) {
            User user = userRepository.findByUserNameIgnoreCase(username)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found: " + username));
            players.add(user);
        }
        return players;
    }

    private TournamentDetailResponse toDetailResponse(Tournament tournament) {
        List<PlayerResponse> playerResponses = tournament.getPlayers().stream()
                .map(u -> PlayerResponse.builder()
                        .userId(u.getId())
                        .username(u.getUserName())
                        .build())
                .collect(Collectors.toList());

        List<MatchResponse> bracket = matchRepository
                .findByTournamentOrderByRoundAscMatchIndexAsc(tournament).stream()
                .map(this::toMatchResponse)
                .collect(Collectors.toList());

        Long activeMatchId = tournament.getActiveMatch() != null
                ? tournament.getActiveMatch().getId()
                : null;

        return TournamentDetailResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .creatorUsername(tournament.getCreator().getUserName())
                .status(tournament.getStatus())
                .champion(tournament.getChampion())
                .players(playerResponses)
                .bracket(bracket)
                .activeMatchId(activeMatchId)
                .build();
    }

    private TournamentSummaryResponse toSummaryResponse(Tournament tournament) {
        return TournamentSummaryResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .creatorUsername(tournament.getCreator().getUserName())
                .status(tournament.getStatus())
                .playerCount(tournament.getPlayers().size())
                .build();
    }

    MatchResponse toMatchResponse(TournamentMatch match) {
        return MatchResponse.builder()
                .id(match.getId())
                .round(match.getRound())
                .matchIndex(match.getMatchIndex())
                .player1Username(match.getPlayer1() != null ? match.getPlayer1().getUserName() : null)
                .player2Username(match.getPlayer2() != null ? match.getPlayer2().getUserName() : null)
                .score1(match.getScore1())
                .score2(match.getScore2())
                .winnerUsername(match.getWinner() != null ? match.getWinner().getUserName() : null)
                .status(match.getStatus())
                .build();
    }
}

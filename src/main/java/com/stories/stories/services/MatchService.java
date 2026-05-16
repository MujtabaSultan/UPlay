package com.stories.stories.services;

import com.stories.stories.models.*;
import com.stories.stories.repositories.TournamentMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final TournamentMatchRepository matchRepository;
    private final TournamentService tournamentService;

    public MatchResponse adjustScore(Long matchId, ScoreAdjustRequest request, User requester) {
        TournamentMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Match not found: " + matchId));

        Tournament tournament = match.getTournament();

        if (!tournament.getCreator().getId().equals(requester.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the tournament creator can perform this action");
        }

        if (tournament.getStatus() == TournamentStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Tournament is already completed");
        }

        if (tournament.getActiveMatch() == null
                || !tournament.getActiveMatch().getId().equals(matchId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only the active match may be scored");
        }

        if (request.getDelta() != 1 && request.getDelta() != -1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delta must be +1 or -1");
        }

        if ("PLAYER1".equals(request.getPlayer())) {
            int newScore = match.getScore1() + request.getDelta();
            if (newScore < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Score cannot be negative");
            }
            match.setScore1(newScore);
        } else if ("PLAYER2".equals(request.getPlayer())) {
            int newScore = match.getScore2() + request.getDelta();
            if (newScore < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Score cannot be negative");
            }
            match.setScore2(newScore);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Player must be PLAYER1 or PLAYER2");
        }

        User winner = null;
        if (isWin(match.getScore1(), match.getScore2())) {
            winner = match.getPlayer1();
        } else if (isWin(match.getScore2(), match.getScore1())) {
            winner = match.getPlayer2();
        }

        matchRepository.save(match);

        if (winner != null) {
            match.setWinner(winner);
            match.setStatus(MatchStatus.COMPLETED);
            tournamentService.advanceBracket(tournament, match, winner);
        }

        return tournamentService.toMatchResponse(match);
    }

    private boolean isWin(int score, int opponentScore) {

        return (score >= 11 && (score - opponentScore) >= 2) || (score == 7 && opponentScore == 0);

    }
}

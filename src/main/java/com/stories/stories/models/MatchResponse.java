package com.stories.stories.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponse {
    private Long id;
    private MatchRound round;
    private int matchIndex;
    private String player1Username;
    private String player2Username;
    private int score1;
    private int score2;
    private String winnerUsername;
    private MatchStatus status;
}

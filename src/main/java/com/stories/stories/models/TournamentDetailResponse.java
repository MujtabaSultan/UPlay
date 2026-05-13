package com.stories.stories.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDetailResponse {
    private Long id;
    private String name;
    private String creatorUsername;
    private TournamentStatus status;
    private String champion;
    private List<PlayerResponse> players;
    private List<MatchResponse> bracket;
    private Long activeMatchId;
}

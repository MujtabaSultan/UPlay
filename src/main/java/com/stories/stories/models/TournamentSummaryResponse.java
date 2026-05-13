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
public class TournamentSummaryResponse {
    private Long id;
    private String name;
    private String creatorUsername;
    private TournamentStatus status;
    private int playerCount;
}

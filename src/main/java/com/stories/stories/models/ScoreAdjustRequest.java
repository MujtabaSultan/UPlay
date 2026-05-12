package com.stories.stories.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScoreAdjustRequest {
    private String player;
    private int delta;
}

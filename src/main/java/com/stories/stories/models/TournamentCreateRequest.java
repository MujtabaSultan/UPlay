package com.stories.stories.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TournamentCreateRequest {
    private String name;
    private int size;
    private List<String> usernames;
}

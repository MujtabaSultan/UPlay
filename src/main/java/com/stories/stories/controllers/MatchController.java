package com.stories.stories.controllers;

import com.stories.stories.models.MatchResponse;
import com.stories.stories.models.ScoreAdjustRequest;
import com.stories.stories.models.User;
import com.stories.stories.services.MatchService;
import com.stories.stories.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final UserService userService;

    @PatchMapping("/{matchId}/score")
    public MatchResponse adjustScore(
            @PathVariable Long matchId,
            @RequestBody ScoreAdjustRequest request) {
        User requester = userService.getUser();
        return matchService.adjustScore(matchId, request, requester);
    }
}

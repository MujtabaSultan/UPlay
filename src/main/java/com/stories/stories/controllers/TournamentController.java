package com.stories.stories.controllers;

import com.stories.stories.models.TournamentCreateRequest;
import com.stories.stories.models.TournamentDetailResponse;
import com.stories.stories.models.TournamentSummaryResponse;
import com.stories.stories.models.User;
import com.stories.stories.services.TournamentService;
import com.stories.stories.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final UserService userService;

    @GetMapping
    public List<TournamentSummaryResponse> getTournaments() {
        return tournamentService.getTournaments();
    }

    @PostMapping
    public TournamentDetailResponse createTournament(@RequestBody TournamentCreateRequest request) {
        User creator = userService.getUser();
        return tournamentService.createTournament(request, creator);
    }

    @GetMapping("/{id}")
    public TournamentDetailResponse getTournament(@PathVariable Long id) {
        return tournamentService.getTournament(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        User requester = userService.getUser();
        tournamentService.deleteTournament(id, requester);
        return ResponseEntity.noContent().build();
    }
}

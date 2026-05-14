package com.stories.stories.repositories;

import com.stories.stories.models.MatchRound;
import com.stories.stories.models.Tournament;
import com.stories.stories.models.TournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {
    List<TournamentMatch> findByTournamentOrderByRoundAscMatchIndexAsc(Tournament tournament);

    Optional<TournamentMatch> findByTournamentAndRoundAndMatchIndex(
            Tournament tournament, MatchRound round, int matchIndex);
}

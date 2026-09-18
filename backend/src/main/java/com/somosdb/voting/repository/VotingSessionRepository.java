package com.somosdb.voting.repository;

import com.somosdb.voting.domain.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VotingSessionRepository extends JpaRepository<VotingSession, UUID> {
    Optional<VotingSession> findByAgendaId(UUID agendaId);
}


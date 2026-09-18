package com.somosdb.voting.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "voting_sessions", uniqueConstraints = @UniqueConstraint(name = "uk_session_agenda", columnNames = "agenda_id"))
public class VotingSession {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;
    @Column(nullable = false, updatable = false)
    private Instant openedAt;
    @Column(nullable = false, updatable = false)
    private Instant closesAt;

    protected VotingSession() {}

    public VotingSession(UUID id, Agenda agenda, Instant openedAt, Instant closesAt) {
        this.id = id;
        this.agenda = agenda;
        this.openedAt = openedAt;
        this.closesAt = closesAt;
    }

    public UUID getId() { return id; }
    public Agenda getAgenda() { return agenda; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getClosesAt() { return closesAt; }
    public boolean isOpenAt(Instant instant) { return !instant.isBefore(openedAt) && instant.isBefore(closesAt); }
}


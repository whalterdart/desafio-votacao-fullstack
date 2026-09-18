package com.somosdb.voting.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes",
        uniqueConstraints = @UniqueConstraint(name = "uk_vote_agenda_associate", columnNames = {"agenda_id", "associate_id"}),
        indexes = @Index(name = "idx_vote_agenda_choice", columnList = "agenda_id, choice"))
public class Vote {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;
    @Column(name = "associate_id", nullable = false, length = 80)
    private String associateId;
    @Column(nullable = false, length = 11)
    private String cpf;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private VoteChoice choice;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Vote() {}

    public Vote(UUID id, Agenda agenda, String associateId, String cpf, VoteChoice choice, Instant createdAt) {
        this.id = id;
        this.agenda = agenda;
        this.associateId = associateId;
        this.cpf = cpf;
        this.choice = choice;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getAssociateId() { return associateId; }
    public VoteChoice getChoice() { return choice; }
    public Instant getCreatedAt() { return createdAt; }
}

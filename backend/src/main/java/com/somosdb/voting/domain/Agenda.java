package com.somosdb.voting.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agendas")
public class Agenda {
    @Id
    private UUID id;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Agenda() {}

    public Agenda(UUID id, String title, String description, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}


package com.somosdb.voting.api.dto;

import java.time.Instant;
import java.util.UUID;

public record AgendaResponse(UUID id, String title, String description, Instant createdAt, SessionResponse session) {}


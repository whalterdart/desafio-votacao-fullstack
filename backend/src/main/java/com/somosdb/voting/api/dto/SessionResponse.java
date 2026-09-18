package com.somosdb.voting.api.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(UUID id, Instant openedAt, Instant closesAt, boolean open) {}


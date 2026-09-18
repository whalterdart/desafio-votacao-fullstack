package com.somosdb.voting.api.dto;

import com.somosdb.voting.domain.VoteChoice;
import java.time.Instant;
import java.util.UUID;

public record VoteResponse(UUID id, String associateId, VoteChoice choice, Instant createdAt) {}


package com.somosdb.voting.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OpenSessionRequest(@Min(1) @Max(86400) Long durationSeconds) {
    public long durationOrDefault() { return durationSeconds == null ? 60 : durationSeconds; }
}


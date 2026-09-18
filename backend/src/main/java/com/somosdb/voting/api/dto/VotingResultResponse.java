package com.somosdb.voting.api.dto;

import java.util.UUID;

public record VotingResultResponse(
        UUID agendaId,
        long yes,
        long no,
        long total,
        String sessionStatus,
        String result
) {}


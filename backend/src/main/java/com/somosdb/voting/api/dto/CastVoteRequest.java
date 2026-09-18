package com.somosdb.voting.api.dto;

import com.somosdb.voting.domain.VoteChoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CastVoteRequest(
        @NotBlank @Size(max = 80) String associateId,
        @NotBlank String cpf,
        @NotNull VoteChoice choice
) {}


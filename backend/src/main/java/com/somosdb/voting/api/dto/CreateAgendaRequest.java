package com.somosdb.voting.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAgendaRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 1000) String description
) {}

